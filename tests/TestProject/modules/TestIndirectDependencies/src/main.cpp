/*
 * Copyright (C) 2013 BMW Car IT GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
 #include "TestIndirectDependencies_Direct/TestIndirectDependencies_Direct.h"
 #include "TestIndirectDependencies_Indirect_Dynamic/TestIndirectDependencies_Indirect_Dynamic.h"
 #include "TestIndirectDependencies_Indirect_ExternalCmake/TestIndirectDependencies_Indirect_ExternalCmake.h"
 #include "TestIndirectDependencies_Indirect_OnlyHeaders/TestIndirectDependencies_Indirect_OnlyHeaders.h"
 #include "TestIndirectDependencies_Indirect_Static/TestIndirectDependencies_Indirect_Static.h"
 #include "TestIndirectDependencies_Indirect_SystemLibrary/TestIndirectDependencies_Indirect_SystemLibrary.h"
 
 class concrete : public TestIndirectDependencies_Indirect_OnlyHeaders
 {
	public:
	void method(){};
 };
 
int main(){
	TestIndirectDependencies_Direct a;
	a.method();
	TestIndirectDependencies_Indirect_Dynamic b;
	b.method();
	TestIndirectDependencies_Indirect_ExternalCmake c;
	c.method();
	concrete d;
	d.method();
	TestIndirectDependencies_Indirect_Static e;
	e.method();
	TestIndirectDependencies_Indirect_SystemLibrary f;
	f.method();
}