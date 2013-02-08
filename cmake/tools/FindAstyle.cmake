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

# This module looks for astyle and defines the following values:
#  ASTYLE_EXECUTABLE: the full path to the astyle executable.
#  ASTYLE_FOUND: True if astyle has been found.

FIND_PROGRAM(ASTYLE_EXECUTABLE
  astyle
  /usr/bin
)

INCLUDE(FindPackageHandleStandardArgs)
FIND_PACKAGE_HANDLE_STANDARD_ARGS(Astyle DEFAULT_MSG ASTYLE_EXECUTABLE)
MARK_AS_ADVANCED( ASTYLE_EXECUTABLE )
SET(ASTYLE_EXECUTABLE ${ASTYLE_EXECUTABLE} CACHE PATH "")

IF(NOT "${ASTYLE_EXECUTABLE}" STREQUAL "ASTYLE_EXECUTABLE-NOTFOUND")
	SET(ASTYLE_FOUND 1 CACHE INTERNAL "")
ENDIF()




