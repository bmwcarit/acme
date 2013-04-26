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

# Fails if trying to download this
# Since nobody has dependency on this target, nothing should happen						

ACME_ADD_CMAKE_PROJECT(	NonExistingProject
    LIBNAMES nonExistingLib
    URL "http://nonexisting.com/something.zip"
    CHECKSUM "7c5709c8bd7f6f93682349fc38797e6f"
    INSTALL 1
)
