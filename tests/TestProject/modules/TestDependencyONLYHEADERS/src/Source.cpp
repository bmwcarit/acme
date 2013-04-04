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

#include "TestDependencyONLYHEADERS_CantLinkDouble/DoubleCantLinkAgainHeader.h"
#include "TestDependencyONLYHEADERS_CantLinkDouble2/DoubleCantLinkAgainHeader.h"
#include <stdio.h>


 int main(){
     DoubleCantLinkAgainClass s;
     s.someNumber = 42;
     DoubleCantLinkAgainClass2 s2;
     s2.someNumber = 42;
     int val = multipleDefinedSymbol();
     printf("%i", val);
     return 0;
 }
 