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

ACME_ADD_SUBDIRECTORY(TestAddInstallFile)
ACME_ADD_SUBDIRECTORY(TestResources)
ACME_ADD_SUBDIRECTORY(TestModuleTypeExe)
ACME_ADD_SUBDIRECTORY(TestModuleTypeStatic)
ACME_ADD_SUBDIRECTORY(TestModuleTypeDynamic)
ACME_ADD_SUBDIRECTORY(TestAddDebugDefinition)
ACME_ADD_SUBDIRECTORY(TestAddReleaseDefinition)
ACME_ADD_SUBDIRECTORY(TestAddDefinition)
ACME_ADD_SUBDIRECTORY(TestDefinitionsSetByACME)
ACME_ADD_SUBDIRECTORY(TestAutomaticTestProjects)
ACME_ADD_SUBDIRECTORY(TestDependencyAutolink_TheDependency)
ACME_ADD_SUBDIRECTORY(TestDependencyAutolink)
ACME_ADD_SUBDIRECTORY(TestDependencyOnAnExternalCmake)
ACME_ADD_SUBDIRECTORY(TestDependencyOnAnExternalACME)
ACME_ADD_SUBDIRECTORY(TestDependencyOnAnExternalACME_withoutBuildingTests)
ACME_ADD_SUBDIRECTORY(TestDependencyOnSystemLibrary)
ACME_ADD_SUBDIRECTORY(TestIndirectDependencies_Indirect_SystemLibrary)
ACME_ADD_SUBDIRECTORY(TestIndirectDependencies_Indirect_Static)
ACME_ADD_SUBDIRECTORY(TestIndirectDependencies_Indirect_OnlyHeaders)
ACME_ADD_SUBDIRECTORY(TestIndirectDependencies_Indirect_ExternalCmake)
ACME_ADD_SUBDIRECTORY(TestIndirectDependencies_Indirect_Dynamic)
ACME_ADD_SUBDIRECTORY(TestIndirectDependencies_Direct)
ACME_ADD_SUBDIRECTORY(TestIndirectDependencies)
ACME_ADD_SUBDIRECTORY(TestDependencyONLYHEADERS_CantLinkDouble)
ACME_ADD_SUBDIRECTORY(TestDependencyONLYHEADERS_CantLinkDouble2)
ACME_ADD_SUBDIRECTORY(TestDependencyONLYHEADERS)
