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
 #ifdef WIN32
    #include <Windows.h>
    #include "Shlwapi.h"
#else
    #include <dlfcn.h>
#endif

int main(){
#ifdef WIN32
    // Requires Shlwapi
    char testString[ ] = "test.txt"; 
    char *pTestString = testString;

    PathRemoveFileSpec(pTestString);
#else
    // Requires dl
    dlopen("test", RTLD_LAZY);
#endif
    return 0;
}