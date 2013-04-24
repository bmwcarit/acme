#
# Copyright (C) 2013 BMW Car IT GmbH
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Build this to run acme tests, ie build the testproject and execute some
# checks on the results of building like paths, targets etc


cmake_minimum_required (VERSION 2.8.9)

project (RunAllTests)

include(ExternalProject)

##############################
# Helper Methods for testing #
##############################
FUNCTION(ASSERT)
	IF(NOT ${ARGV})
		MESSAGE(SEND_ERROR " FAIL.")
	ELSE()
		MESSAGE(" SUCCESS.")
	ENDIF()
ENDFUNCTION(ASSERT)

FUNCTION(ASSERT_FALSE)
	IF(${ARGV})
		MESSAGE(SEND_ERROR " FAIL.")
	ELSE()
		MESSAGE(" SUCCESS.")
	ENDIF()
ENDFUNCTION(ASSERT_FALSE)

FUNCTION(TEST_BUILD TargetName Description)
	MESSAGE(${Description})

	execute_process(COMMAND cmake --build . --target ${TargetName} --config ${CURRENT_CONFIG}
		WORKING_DIRECTORY "${CURRENT_BUILD_DIR}"
		OUTPUT_VARIABLE output
		ERROR_VARIABLE output
		RESULT_VARIABLE retValue)

	ASSERT(retValue EQUAL 0 "")
	IF(NOT retValue EQUAL 0)
		MESSAGE("Output: ${output}")
	ENDIF()
ENDFUNCTION(TEST_BUILD)

FUNCTION(DELETE_FILE FileToDelete)
execute_process(COMMAND cmake -E remove "${FileToDelete}"
			OUTPUT_VARIABLE output
			ERROR_VARIABLE output
			RESULT_VARIABLE retValue)
MESSAGE("Deleting file: ${FileToDelete}")
	ASSERT(retValue EQUAL 0 "")
ENDFUNCTION(DELETE_FILE)

FUNCTION(CONFIGURE_TESTPROJECT)
	MESSAGE("Create build directory: ${CURRENT_BUILD_DIR}")
	execute_process(COMMAND cmake -E make_directory "${CURRENT_BUILD_DIR}"
			OUTPUT_VARIABLE output
			ERROR_VARIABLE output
			RESULT_VARIABLE retValue)
	ASSERT(retValue EQUAL 0 "")

	MESSAGE("Testproject configure (config: ${CURRENT_CONFIG}, prefix: ${CURRENT_INSTALL_PREFIX})")
	IF(NOT ${CURRENT_INSTALL_PREFIX} STREQUAL "")
		SET(INSTALL_DIRECTIVE -DCMAKE_INSTALL_PREFIX=${CURRENT_INSTALL_PREFIX})
		MESSAGE("Using CMAKE_INSTALL_PREFIX: ${CURRENT_INSTALL_PREFIX}")
	ENDIF()
	
	execute_process(COMMAND cmake --config ${CURRENT_CONFIG} ${PROJECT_SOURCE_DIR}/TestProject -DCMAKE_TOOLCHAIN_FILE=${TOOLCHAINFILE} -DCONFIG_VERBOSE=1 ${INSTALL_DIRECTIVE} -DCMAKE_BUILD_TYPE=${CURRENT_CONFIG}
			WORKING_DIRECTORY "${CURRENT_BUILD_DIR}"
			OUTPUT_VARIABLE output
			ERROR_VARIABLE output
			RESULT_VARIABLE retValue)
	ASSERT(retValue EQUAL 0)
	IF(NOT retValue EQUAL 0)
		MESSAGE("Output: ${output}")
	ENDIF()
ENDFUNCTION(CONFIGURE_TESTPROJECT)

FUNCTION(RUN_TEST NAME)
	SET(executableFilename "${CURRENT_BUILD_DIR}/TestProject/${OSNAME}_X86_32/${CURRENT_CONFIG}/bin/${NAME}")
	MESSAGE("Run test: ${executableFilename}")
	execute_process(COMMAND ${executableFilename}
			OUTPUT_VARIABLE output
			ERROR_VARIABLE output
			RESULT_VARIABLE retValue)
	ASSERT(retValue EQUAL 0)
	IF(NOT retValue EQUAL 0)
		MESSAGE("Output: ${output}")
	ENDIF()
ENDFUNCTION(RUN_TEST)

#Choose variables and toolchain file based on os
IF(UNIX)
    SET(OSNAME "Linux")
	SET(TOOLCHAINFILE "${PROJECT_SOURCE_DIR}/../cmake/example_toolchains/nativeLinux_X86_32.toolchain")
ELSEIF(WIN32)
    SET(OSNAME "Windows")
	SET(TOOLCHAINFILE "${PROJECT_SOURCE_DIR}/../cmake/example_toolchains/nativeWindows_X86_32.toolchain" CACHE FILEPATH "")
ENDIF()

MESSAGE("Using toolchain for tests: ${TOOLCHAINFILE}")
INCLUDE(${TOOLCHAINFILE})
MESSAGE("Running acme tests from ${PROJECT_SOURCE_DIR}")
MESSAGE("-------------------------")

FUNCTION(TestResourceAndInstallFilesWithConfiguration)
    MESSAGE("Testing resources and install files with configuration: ${CURRENT_CONFIG} and prefix: ${CURRENT_INSTALL_PREFIX}")

    TEST_BUILD(TestResources "Files in /res are copied to build directory (${CURRENT_CONFIG})")
    MESSAGE("Resources exists in build directory")
    SET(resFileInBuildDir "${CURRENT_BUILD_DIR}/TestProject/${OSNAME}_X86_32/${CURRENT_CONFIG}/res/TestResources/AResourceFile.txt")
    ASSERT(EXISTS ${resFileInBuildDir})

    TEST_BUILD(TestAddInstallFile "Add install file (${CURRENT_CONFIG})")
    MESSAGE("Install files exists in build directory")
	MESSAGE("Relative path")
    SET(InstallFile "${CURRENT_BUILD_DIR}/TestProject/${OSNAME}_X86_32/${CURRENT_CONFIG}/bin/testInstallFileRelativePath.txt")
    ASSERT(EXISTS ${InstallFile})
	
	MESSAGE("Absolute path")
	SET(InstallFile "${CURRENT_BUILD_DIR}/TestProject/${OSNAME}_X86_32/${CURRENT_CONFIG}/bin/testInstallFileAbsolutePath.txt")
    ASSERT(EXISTS ${InstallFile})
	
	MESSAGE("Custom install location in bin")
	SET(InstallFile "${CURRENT_BUILD_DIR}/TestProject/${OSNAME}_X86_32/${CURRENT_CONFIG}/bin/subDirInBin/testInstallFileToCustomLocation.txt")
    ASSERT(EXISTS ${InstallFile})

    TEST_BUILD(install "Install project into given installprefix")
    MESSAGE("Resources are installed (${CURRENT_CONFIG})")
    SET(resFileInInstallDir "${PROJECT_SOURCE_DIR}/TestProject/deliverable/${OSNAME}_X86_32/${CURRENT_CONFIG}/res/TestResources/AResourceFile.txt")
    ASSERT(EXISTS ${resFileInInstallDir})
    
    MESSAGE("Install files are installed (${CURRENT_CONFIG})")

    SET(InstallFileAfterInstall "${PROJECT_SOURCE_DIR}/TestProject/deliverable/${OSNAME}_X86_32/${CURRENT_CONFIG}/bin/testInstallFileRelativePath.txt")
    ASSERT(EXISTS ${InstallFileAfterInstall})

	SET(InstallFileAfterInstall "${PROJECT_SOURCE_DIR}/TestProject/deliverable/${OSNAME}_X86_32/${CURRENT_CONFIG}/bin/testInstallFileAbsolutePath.txt")
    ASSERT(EXISTS ${InstallFileAfterInstall})

	SET(InstallFileAfterInstall "${PROJECT_SOURCE_DIR}/TestProject/deliverable/${OSNAME}_X86_32/${CURRENT_CONFIG}/bin/subDirInBin/testInstallFileToCustomLocation.txt")
    ASSERT(EXISTS ${InstallFileAfterInstall})
ENDFUNCTION(TestResourceAndInstallFilesWithConfiguration)

##############################
# Preparations               #
##############################

# Build an external library to be used for testing Add_External_library method
MESSAGE("Create test external library")
execute_process(COMMAND cmake -E make_directory "${PROJECT_BINARY_DIR}/ExternalLibraryBuild"
        OUTPUT_VARIABLE output
        ERROR_VARIABLE output
        RESULT_VARIABLE retValue)
ASSERT(retValue EQUAL 0 "")

execute_process(COMMAND cmake --config Release ${PROJECT_SOURCE_DIR}/ExternalLibrary -DCMAKE_INSTALL_PREFIX=${PROJECT_SOURCE_DIR}/ExternalLibrary/built/${OSNAME}_Release -DCMAKE_BUILD_TYPE=Release
        WORKING_DIRECTORY "${PROJECT_BINARY_DIR}/ExternalLibraryBuild"
        OUTPUT_VARIABLE output
        ERROR_VARIABLE output
        RESULT_VARIABLE retValue)
ASSERT(retValue EQUAL 0)
IF(NOT retValue EQUAL 0)
    MESSAGE("Output: ${output}")
ENDIF()

execute_process(COMMAND cmake --build . --target install --config Release
		WORKING_DIRECTORY "${PROJECT_BINARY_DIR}/ExternalLibraryBuild"
		OUTPUT_VARIABLE output
		ERROR_VARIABLE output
		RESULT_VARIABLE retValue)
ASSERT(retValue EQUAL 0)
IF(NOT retValue EQUAL 0)
    MESSAGE("Output: ${output}")
ENDIF()

##############################
# Run tests                  #
##############################

## DEBUG TESTS
SET(CURRENT_BUILD_DIR ${PROJECT_BINARY_DIR}/TestProjectBuild_Debug)
SET(CURRENT_CONFIG Debug)
SET(CURRENT_INSTALL_PREFIX "")
CONFIGURE_TESTPROJECT()

TEST_BUILD(TestAddExternalLibrary "TestAddExternalLibrary")
TestResourceAndInstallFilesWithConfiguration()

TEST_BUILD(TestAddDebugDefinitionTest "AddDebugDefinition")
RUN_TEST(TestAddDebugDefinitionTest)

## RELEASE TESTS
SET(CURRENT_CONFIG Release)
SET(CURRENT_BUILD_DIR ${PROJECT_BINARY_DIR}/TestProjectBuild_Release)
CONFIGURE_TESTPROJECT()
TestResourceAndInstallFilesWithConfiguration()

TEST_BUILD(TestModuleTypeExe "Simple executable from sources")
TEST_BUILD(TestModuleTypeStatic "Simple static library from sources")
TEST_BUILD(TestModuleTypeDynamic "Simple dynamic library from sources")
TEST_BUILD(TestAddReleaseDefinitionTest "AddReleaseDefinition")
RUN_TEST(TestAddReleaseDefinitionTest)
TEST_BUILD(TestAddDefinition "AddDefinition (build Debug)")
TEST_BUILD(TestAddDefinition "AddDefinition (build Release)")
TEST_BUILD(TestDefinitionsSetByACME "TARGET_OS is set by acme")
TEST_BUILD(TestAutomaticTestProjectsTest "Testprojects are added automatically when appropriate files exist")
TEST_BUILD(TestDependencyAutolink "ACME_ADD_DEPENDENCY includes header path and links automatically")
TEST_BUILD(TestDependencyOnAnExternalCmake "Test add dependency which is an external cmake project with a find script in (project)/cmake/plugins")
TEST_BUILD(TestDependencyOnSystemLibrary "Test add dependency which is a system library with a find script in (project)/cmake/modules")
TEST_BUILD(TestIndirectDependencies_Indirect_SystemLibrary "Indirect dependency on module depending on system library")
TEST_BUILD(TestIndirectDependencies_Indirect_Static "Indirect dependency on static library module")
TEST_BUILD(TestIndirectDependencies_Indirect_OnlyHeaders "Indirect dependency on module containing only headers")
TEST_BUILD(TestIndirectDependencies_Indirect_ExternalCmake "Indirect dependency on module depending on external cmake")
TEST_BUILD(TestIndirectDependencies_Indirect_Dynamic "Indirect dependency on dynamic library module")
TEST_BUILD(TestIndirectDependencies_Direct "Start of indirect dependency chain")
TEST_BUILD(TestIndirectDependencies "Indirect dependency chain an multiple projects in a row of different types")
TEST_BUILD(TestDependencyOnAnExternalACME "Dependency on another ACME project")
MESSAGE("Tests exist in external ACME Project")
ASSERT(EXISTS "${CURRENT_BUILD_DIR}/externals/bin/ExternalACMEModuleTest${CMAKE_EXECUTABLE_SUFFIX}")

TEST_BUILD(TestDependencyOnAnExternalACME_withoutBuildingTests "Dependency on another ACME project, without building tests in it")
MESSAGE("Lib of external ACME Project without tests exists")
ASSERT(EXISTS "${CURRENT_BUILD_DIR}/externals/lib/${CMAKE_STATIC_LIBRARY_PREFIX}ExternalACMEModule2${CMAKE_STATIC_LIBRARY_SUFFIX}")
MESSAGE("Tests do not exist in external ACME Project which was configured not to build tests")
ASSERT_FALSE(EXISTS "${CURRENT_BUILD_DIR}/externals/bin/ExternalACMEModule2Test${CMAKE_EXECUTABLE_SUFFIX}")

TEST_BUILD(TestDependencyONLYHEADERS "ADD_DEPENDENCY(X ONLY_HEADERS) does not link other module")

SET(ExpectedFilename "${CURRENT_BUILD_DIR}/externals/lib/${CMAKE_STATIC_LIBRARY_PREFIX}ExternalACMEModule${CMAKE_STATIC_LIBRARY_SUFFIX}")
message("file: ${ExpectedFilename}")
MESSAGE("ACME lib exists")
ASSERT(EXISTS ${ExpectedFilename})

SET(DeliverableFilename "${PROJECT_SOURCE_DIR}/TestProject/deliverable/${OSNAME}_X86_32/${CURRENT_CONFIG}/lib/${CMAKE_STATIC_LIBRARY_PREFIX}ExternalACMEModule${CMAKE_STATIC_LIBRARY_SUFFIX}")
TEST_BUILD(install "Install project")
MESSAGE("ACME lib exists in deliverable after install")
ASSERT(EXISTS ${DeliverableFilename})

SET(CURRENT_BUILD_DIR ${PROJECT_BINARY_DIR}/TestProjectBuildWithInstallPrefix)
SET(CURRENT_CONFIG "Release")
SET(CURRENT_INSTALL_PREFIX "${PROJECT_SOURCE_DIR}/MyInstallPrefix")
CONFIGURE_TESTPROJECT()

TEST_BUILD(install "Install project into given installprefix")
MESSAGE("ACME lib exists in chosen installprefix after install")
SET(DeliverableFilename "${CURRENT_INSTALL_PREFIX}/lib/${CMAKE_STATIC_LIBRARY_PREFIX}ExternalACMEModule${CMAKE_STATIC_LIBRARY_SUFFIX}")
ASSERT(EXISTS ${DeliverableFilename})
