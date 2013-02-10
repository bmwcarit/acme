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
 
#include <HelloWriterTest.h>

void HelloWriterTest::SetUp()
{
	helloWriter = new HelloWriter();
}

void HelloWriterTest::TearDown()
{
	delete helloWriter;
}

TEST_F(HelloWriterTest, WriteHello)
{
	EXPECT_STREQ("Hello ACME", helloWriter->getHelloMessage());
}

TEST_F(HelloWriterTest, WriteAddMessage)
{
	char* buffer;
	helloWriter->writeAddMessage(3, 4, &buffer);
	
	EXPECT_STREQ("3 + 4 = 7", buffer);
	
	delete buffer;
}
