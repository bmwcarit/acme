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


cmake_minimum_required (VERSION 2.8.10)

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

FUNCTION(CONFIGURE_TESTPROJECT)
	SET(BUILD_DIR_WITH ${CURRENT_BUILD_DIR}/${CURRENT_CONFIG})
	MESSAGE("Create build directory: ${BUILD_DIR_WITH}")
	execute_process(COMMAND cmake -E make_directory "${BUILD_DIR_WITH}"
			OUTPUT_VARIABLE output
			ERROR_VARIABLE output
			RESULT_VARIABLE retValue)
	ASSERT(retValue EQUAL 0 "")

	MESSAGE("Testproject configure")
	IF(NOT ${CURRENT_INSTALL_PREFIX} STREQUAL "")
		SET(INSTALL_DIRECTIVE -DCMAKE_INSTALL_PREFIX=${CURRENT_INSTALL_PREFIX})
		MESSAGE("Using CMAKE_INSTALL_PREFIX: ${CURRENT_INSTALL_PREFIX}")
	ENDIF()
	
	execute_process(COMMAND cmake --config ${CURRENT_CONFIG} ../../TestProject -DCMAKE_TOOLCHAIN_FILE=${TOOLCHAINFILE} ${INSTALL_DIRECTIVE} -DCMAKE_BUILD_TYPE=${CURRENT_CONFIG} 
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
	
	IF(LINUX)
	ELSE(WINDOWS)
		SET(executableFilename "${CURRENT_BUILD_DIR}/TestProject/bin/Windows_X86_32/${CURRENT_CONFIG}/${NAME}")
	ENDIF()
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

#Choose an example toolchain file based on os
IF(LINUX)
	SET(TOOLCHAINFILE "${CMAKE_CURRENT_SOURCE_DIR}/../cmake/example_toolchains/nativeLinuxExample.toolchain")
ELSE(WINDOWS)
	SET(TOOLCHAINFILE "${CMAKE_CURRENT_SOURCE_DIR}/../cmake/example_toolchains/nativeWindows_X86_32.toolchain" CACHE FILEPATH "")
ENDIF()

MESSAGE("Using toolchain for tests: ${TOOLCHAINFILE}")
INCLUDE(${TOOLCHAINFILE})
MESSAGE("Running acme tests from ${CMAKE_CURRENT_SOURCE_DIR}")
MESSAGE("-------------------------")
##############################
# Run tests                  #
##############################

## DEBUG TESTS
SET(CURRENT_BUILD_DIR ${PROJECT_BINARY_DIR}/TestProjectBuild)
SET(CURRENT_CONFIG Debug)
SET(CURRENT_INSTALL_PREFIX "")
CONFIGURE_TESTPROJECT()
TEST_BUILD(TestAddDebugDefinitionTest "AddDebugDefinition")
RUN_TEST(TestAddDebugDefinitionTest)

SET(CURRENT_CONFIG "Release")
CONFIGURE_TESTPROJECT()
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

SET(ExpectedFilename "${CURRENT_BUILD_DIR}/externals/lib/${CMAKE_STATIC_LIBRARY_PREFIX}ExternalACMEModule${CMAKE_STATIC_LIBRARY_SUFFIX}")
message("file: ${ExpectedFilename}")
MESSAGE("ACME lib exists")
ASSERT(EXISTS ${ExpectedFilename})

MESSAGE("ACME lib does not exist in deliverable before install")
SET(DeliverableFilename "${CURRENT_BUILD_DIR}/../../TestProject/deliverable/Windows_X86_32/lib/${CMAKE_STATIC_LIBRARY_PREFIX}ExternalACMEModule${CMAKE_STATIC_LIBRARY_SUFFIX}")
ASSERT_FALSE(EXISTS ${DeliverableFilename})

TEST_BUILD(install "Install project")
MESSAGE("ACME lib exists in deliverable after install")
ASSERT(EXISTS ${DeliverableFilename})

SET(CURRENT_BUILD_DIR ${PROJECT_BINARY_DIR}/TestProjectBuildWithInstallPrefix)
SET(CURRENT_CONFIG "Release")
SET(CURRENT_INSTALL_PREFIX "${PROJECT_BINARY_DIR}/../MyInstallPrefix")
CONFIGURE_TESTPROJECT()

TEST_BUILD(install "Install project into given installprefix")
MESSAGE("ACME lib exists in chosen installprefix after install")
SET(DeliverableFilename "${CURRENT_INSTALL_PREFIX}/lib/${CMAKE_STATIC_LIBRARY_PREFIX}ExternalACMEModule${CMAKE_STATIC_LIBRARY_SUFFIX}")
ASSERT(EXISTS ${DeliverableFilename})
