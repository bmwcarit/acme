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
cmake_minimum_required(VERSION 2.8)


PROJECT(HelloACME)

SET(CMAKE_INSTALL_PREFIX "${CMAKE_SOURCE_DIR}/deliverable" CACHE PATH "" FORCE)

FIND_PATH(ACME_ROOT_DIR acme.cmake PATHS "${ACME_ROOT_DIR}") 

IF(NOT "ACME_ROOT_DIR-NOTFOUND" STREQUAL "${ACME_ROOT_DIR}")
	
	INCLUDE("${ACME_ROOT_DIR}/acme.cmake")

	ADD_SUBDIRECTORY(modules)
ELSE()
	MESSAGE(FATAL_ERROR "ACME_ROOT_DIR not set. Specify the path to your ACME installation")
ENDIF()

ACME_FINALIZE()