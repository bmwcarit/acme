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

ACME_ADD_MODULE(TestAddReleaseDefinition static)

ACME_ADD_FILE(ReturnDefinition)

# This tests if only appropriate definitions are set
ACME_ADD_DEBUG_DEFINITION(TEST_DEFINITION=123456789)
ACME_ADD_RELEASE_DEFINITION(TEST_DEFINITION=98765)
ACME_ADD_DEBUG_DEFINITION(TEST_DEFINITION=123456789)
