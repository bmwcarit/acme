/*
 * Copyright 2012 BMW Car IT GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.bmw.carit.acme.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ArgumentTests
{
    @Test
    public void testSubarray1()
    {
        String[] data = { "1", "2", "3", "4", "5" };
        String[] res = ArgumentUtils.subArray(data, 2, 3);
        assertEquals(3, res.length);
        assertEquals("3", res[0]);
        assertEquals("4", res[1]);
        assertEquals("5", res[2]);
    }

    @Test
    public void testSubarray2()
    {
        String[] data = { "1", "2", "3", "4", "5" };
        String[] res = ArgumentUtils.subArray(data, 4, 1);
        assertEquals(1, res.length);
        assertEquals("5", res[0]);
    }

    @Test
    public void testSubarray3()
    {
        String[] data = { "1", "2", "3", "4", "5" };
        String[] res = ArgumentUtils.subArray(data, 0, 2);
        assertEquals(2, res.length);
        assertEquals("1", res[0]);
        assertEquals("2", res[1]);
    }

    @Test
    public void testArgument()
    {
        String val = ArgumentUtils.argument(new String[] { "0" }, 1, "default");
        assertEquals("default", val);

        val = ArgumentUtils.argument(new String[] { "0", "1" }, 1, "default");
        assertEquals("1", val);

        val = ArgumentUtils.argument(new String[] { "0", "1" }, 5, "default");
        assertEquals("default", val);
    }

    @Test
    public void testHasArgument()
    {
        assertTrue(ArgumentUtils.hasArgument(new String[] { "a" }, "a"));
        assertTrue(ArgumentUtils.hasArgument(new String[] { "A" }, "a"));
        assertFalse(ArgumentUtils.hasArgument(new String[] { "A", "b" }, "c"));
        assertTrue(ArgumentUtils.hasArgument(new String[] { "A", "b" }, "b"));
    }
}
