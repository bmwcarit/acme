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

ACME_ADD_CMAKE_PROJECT( ExternalACMEProject
				LIBNAMES	"ExternalACMEModule"
#				LIBDIRS "<libdir>" ...
				INCLUDE_DIRS "."	# (relative from "${PROJECT_SOURCE_DIR}/build/<project_name>")
#				BINARY_INCLUDE_DIRS "<incl_dir>" ...     (relative from "${PROJECT_SOURCE_DIR}/build/<project_name>/src/<project_name>-build/"
#				ABSOLUTE_INCLUDE_DIRS "<incl_dir>" ...   (absolute path)
#				URL "<url>"
#				CHECKSUM "<url_md5>"
				SOURCE_DIR "${PROJECT_SOURCE_DIR}/../ExternalACMEProject" 
#				CMAKE_ARGUMENTS "<argument_name>:<typ>=<value>" ...
#				DEPENDENT_DEFINITIONS "<definition1>" ...
#				DEPENDENT_DEBUG_DEFINITIONS "<definition2>" ...
#				DEPENDENT_RELEASE_DEFINITIONS "<definition3>" ...
				INSTALL	1
			)