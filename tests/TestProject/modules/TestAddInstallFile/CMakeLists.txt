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

ACME_ADD_MODULE(TestAddInstallFile static)

ACME_ADD_FILE(main)

#test absolute path
ACME_ADD_INSTALL_FILE("${CMAKE_CURRENT_SOURCE_DIR}/testInstallFileAbsolutePath.txt")

#test relative path
ACME_ADD_INSTALL_FILE("testInstallFileRelativePath.txt")

#test relative path copied to sub directory in bin
ACME_ADD_INSTALL_FILE("testInstallFileToCustomLocation.txt" subDirInBin)
