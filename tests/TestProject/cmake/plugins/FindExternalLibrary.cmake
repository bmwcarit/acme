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

IF(WIN32)
    SET(FolderName Windows)
ELSE()
    #UNIX
    SET(FolderName Linux)
ENDIF()

ACME_ADD_EXTERNAL_LIBRARY( ExternalLibrary
				LIBNAMES	"ExternalLibraryLib"
				LIBRARY_DIRS "${PROJECT_SOURCE_DIR}/../ExternalLibrary/built/${FolderName}_Release/lib"
				INCLUDE_DIRS "${PROJECT_SOURCE_DIR}/../ExternalLibrary"	# (relative from "${PROJECT_SOURCE_DIR}/build/<project_name>")
				SOURCE_DIR "${PROJECT_SOURCE_DIR}/../ExternalLibrary" 
			)