/*
 * Copyright (C) 2012 BMW Car IT GmbH
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

#include <HelloLib/HelloWriter.h>

#include <Calculator/Calculator.h>

#include <stdio.h>

const char* HelloWriter::getHelloMessage()
{
	return "Hello ACME";
}

void HelloWriter::writeAddMessage(const int a, const int b, char** buffer)
{
	*buffer = new char[64];
	Calculator calculator;
	sprintf_s(*buffer, 64, "%i + %i = %i", a, b, calculator.add(a,b));
}