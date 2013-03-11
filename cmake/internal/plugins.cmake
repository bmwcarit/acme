#
# Copyright (C) 2012 BMW Car IT GmbH
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

IF(NOT DEFINED THIRD_PARTY_DIR)
	SET(THIRD_PARTY_DIR ${PROJECT_SOURCE_DIR}/3psw)
ENDIF()


include(ExternalProject)
find_package(PkgConfig)

MESSAGE(STATUS "Loading plugins")

FILE(GLOB_RECURSE ACME_PLUGINS ${ACME_PATH}/plugins/*.cmake)
FOREACH(plugin ${ACME_PLUGINS})
	MESSAGE(VERBOSE "Found acme plugin ${plugin}")
	INCLUDE(${plugin})
ENDFOREACH()

FILE(GLOB_RECURSE EXTERN_PLUGINS ${CMAKE_SOURCE_DIR}/cmake/plugins/*.cmake)
FOREACH(plugin ${EXTERN_PLUGINS})
	MESSAGE(VERBOSE "Found internal plugin ${plugin}")
	GET_FILENAME_COMPONENT(PLUGIN_NAME ${plugin} NAME_WE)
	STRING(TOUPPER ${PLUGIN_NAME} PLUGIN_NAME)
	INCLUDE(${plugin})
ENDFOREACH()


#IF(${CONFIG_BUILD_UNITTESTS})
#	include(ExternalProject)
#	ExternalProject_Add(
#		GoogleTest
#		PREFIX  ${CMAKE_BINARY_DIR}/GoogleTest
#		DOWNLOAD_DIR ${THIRD_PARTY_DIR}/GoogleTest
#		URL http://googletest.googlecode.com/files/gtest-1.6.0.zip
#		URL_MD5 4577b49f2973c90bf9ba69aa8166b786 
#		CMAKE_ARGS 	-DCMAKE_ARCHIVE_OUTPUT_DIRECTORY:PATH=${THIRD_PARTY_DIR}/libs/gtest
#					-DCMAKE_LIBRARY_OUTPUT_DIRECTORY:PATH=${THIRD_PARTY_DIR}/libs/gtest
#					-DCMAKE_TOOLCHAIN_FILE:PATH=${CMAKE_TOOLCHAIN_FILE}
#		INSTALL_COMMAND ""
#	) 
#	SET(GTEST_DIR "${CMAKE_BINARY_DIR}/GoogleTest/src/GoogleTest")
#	SET(GTEST_INCLUDE_DIR "${GTEST_DIR}/include")
#	SET(GTEST_LIB_DIR "${THIRD_PARTY_DIR}/libs/gtest")
#	
#	ExternalProject_Add(
#		GoogleMock
#		PREFIX  ${CMAKE_BINARY_DIR}/GoogleMock
#		DOWNLOAD_DIR ${THIRD_PARTY_DIR}/GoogleMock
#		URL http://googlemock.googlecode.com/files/gmock-1.6.0.zip
#		URL_MD5 f547f47321ca88d3965ca2efdcc2a3c1
#		CMAKE_ARGS 	-DCMAKE_ARCHIVE_OUTPUT_DIRECTORY:PATH=${THIRD_PARTY_DIR}/libs/gmock
#					-DCMAKE_LIBRARY_OUTPUT_DIRECTORY:PATH=${THIRD_PARTY_DIR}/libs/gmock
#					-DCMAKE_TOOLCHAIN_FILE:PATH=${CMAKE_TOOLCHAIN_FILE}
#		INSTALL_COMMAND ""
#	) 
#	SET(GMOCK_DIR "${CMAKE_BINARY_DIR}/GoogleMock/src/GoogleMock" )
#	SET(GMOCK_INCLUDE_DIR "${GMOCK_DIR}/include" )
#	SET(GMOCK_LIB_DIR "${THIRD_PARTY_DIR}/libs/gmock")
#	
#ENDIF(${CONFIG_BUILD_UNITTESTS})