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
 
#include "ModuleA/ModuleAPublic.h"
#include "ModuleThatDependsOnASystemLibrary/ClassThatUsesThread.h"
#include "ModuleThatDependsOnAnExternalCmake/ClassThatUsesExternalLib.h"
#include "ModuleThatOnlyHasPublicHeaders/APublicHeader.h"

class Concrete : public APublicInterface
{
    public:
	virtual void someInterfaceMethod(){};
};



int main(){
	ModuleAPublic a;
	int value = a.doSomething();

    ClassThatUsesThread thread;
    thread.runAThread();

    ClassThatUsesExternalLib externalLib;
    externalLib.useExternalLib();
	
	Concrete c;
	c.someInterfaceMethod();
}