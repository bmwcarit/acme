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

#include <CalculatorTest.h>


void CalculatorTest::SetUp()
{
	calculator = new Calculator();
}

void CalculatorTest::TearDown()
{
	delete calculator;
}

TEST_F(CalculatorTest, Add)
{
	EXPECT_EQ(7, calculator->add(3, 4));
}

TEST_F(CalculatorTest, Sub)
{
	EXPECT_EQ(-1, calculator->sub(3, 4));
}