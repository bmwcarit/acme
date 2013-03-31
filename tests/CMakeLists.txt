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
		MESSAGE(" FAIL.")
	ELSE()
		MESSAGE(" SUCCESS.")
	ENDIF()
ENDFUNCTION(ASSERT)

FUNCTION(ASSERT_FALSE)
math(EXPR lastpos "${ARGC} - 1")
	#LIST(GET ARGV "${lastpos}" MESSAGETEXT)
	#LIST(REMOVE_AT ARGV "${lastpos}")
	IF(${ARGV})
		MESSAGE(" FAIL.")
	ELSE()
		MESSAGE(" SUCCESS.")
	ENDIF()
ENDFUNCTION(ASSERT_FALSE)

FUNCTION(ASSERT_NOTEMPTY)
	#math(EXPR lastpos "${ARGC} - 1")
	#LIST(GET ARGV "${lastpos}" MESSAGETEXT)
	#LIST(REMOVE_AT ARGV "${lastpos}")
	IF(${ARGV} STREQUAL "")
		MESSAGE(" FAIL.") # current LINE/MESSAGE??
	ELSE()
		MESSAGE(" SUCCESS.")
	ENDIF()
ENDFUNCTION(ASSERT_NOTEMPTY)

FUNCTION(TEST_BUILD_TYPE TargetName Description CONFIG)
	MESSAGE(${Description})

	execute_process(COMMAND cmake --build . --target ${TargetName} --config ${CONFIG}
		WORKING_DIRECTORY "${PROJECT_BINARY_DIR}/TestProjectBuild"
		OUTPUT_VARIABLE output
		ERROR_VARIABLE output
		RESULT_VARIABLE retValue)

	ASSERT(retValue EQUAL 0 "")
	IF(NOT retValue EQUAL 0)
		MESSAGE("Output: ${output}")
	ENDIF()
ENDFUNCTION(TEST_BUILD_TYPE)

FUNCTION(TEST_BUILD TargetName Description)
	TEST_BUILD_TYPE(${TargetName} ${Description} Release)
ENDFUNCTION(TEST_BUILD)

#Choose an example toolchain file based on os
IF(LINUX)
	SET(TOOLCHAINFILE "${CMAKE_CURRENT_SOURCE_DIR}/../cmake/example_toolchains/nativeLinuxExample.toolchain")
ELSE(WINDOWS)
	SET(TOOLCHAINFILE "${CMAKE_CURRENT_SOURCE_DIR}/../cmake/example_toolchains/nativeWindows_X86_32.toolchain" CACHE FILEPATH "") #"${CMAKE_CURRENT_SOURCE_DIR}/../cmake/example_toolchains/nativeWindows_X86_32.toolchain")
ENDIF()

MESSAGE("Using toolchain for tests: ${TOOLCHAINFILE}")
INCLUDE(${TOOLCHAINFILE})
MESSAGE("Running acme tests from ${CMAKE_CURRENT_SOURCE_DIR}")
MESSAGE("-------------------------")
##############################
# Run tests                  #
##############################
MESSAGE("Create build directory")
execute_process(COMMAND cmake -E make_directory ${PROJECT_BINARY_DIR}/TestProjectBuild
		OUTPUT_VARIABLE output
		ERROR_VARIABLE output
		RESULT_VARIABLE retValue)
ASSERT(retValue EQUAL 0 "")

MESSAGE( "Testproject configure")
SET(WORKING_DIR "${PROJECT_BINARY_DIR}/TestProjectBuild")
execute_process(COMMAND cmake ../../TestProject -DCMAKE_TOOLCHAIN_FILE=${TOOLCHAINFILE}
		WORKING_DIRECTORY "${WORKING_DIR}"
		OUTPUT_VARIABLE output
		ERROR_VARIABLE output
		RESULT_VARIABLE retValue)
ASSERT(retValue EQUAL 0)
IF(NOT retValue EQUAL 0)
MESSAGE("Output: ${output}")
ENDIF()


TEST_BUILD(TestModuleTypeExe "Simple executable from sources")