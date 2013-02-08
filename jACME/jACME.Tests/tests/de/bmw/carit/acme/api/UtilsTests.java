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

package de.bmw.carit.acme.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.AfterClass;
import org.junit.Test;

import de.bmw.carit.acme.cli.ConsoleOutput;

public class UtilsTests
{

    private static final String TESTPR_DIR = "testdir";
    private static final String TESTPR_NAME = "testpr";

    @AfterClass
    public static void after()
    {
        FileUtils.deleteDirectory(TESTPR_DIR);
    }

    @Test
    public void testProjectName()
    {
        ACME.createProject(TESTPR_DIR, TESTPR_NAME, new ConsoleOutput());
        String name = FileUtils.getProjectName(new File(TESTPR_DIR, TESTPR_NAME).toString());
        assertEquals(TESTPR_NAME, name);
    }

    @Test
    public void testFilenameExtensionRemove()
    {
        assertEquals("test", FileUtils.removeExtension("test.h"));
        assertEquals("test", FileUtils.removeExtension("test.cpp"));
    }

    @Test
    public void testModulesDir()
    {
        // must run after 'testProjectName'
        File modulesDir = FileUtils.findModulesDir(new File(TESTPR_DIR, TESTPR_NAME).toString());
        assertNotNull(modulesDir);
        assertEquals(Constants.MODULES_FOLDERNAME, modulesDir.getName());
    }

    @Test
    public void testNameOk()
    {
        assertTrue(FileUtils.isNameOk("normal4321"));
        assertTrue(FileUtils.isNameOk("Normal1234"));
        assertFalse(FileUtils.isNameOk(" "));
        assertFalse(FileUtils.isNameOk("/"));
        assertFalse(FileUtils.isNameOk("asdf/asdf"));
        assertFalse(FileUtils.isNameOk("asdf#asdf"));
        assertFalse(FileUtils.isNameOk("asdf\\asdf"));
    }

    @Test
    public void testRemoveTest()
    {
        assertEquals("Test", FileUtils.removeTest("Test"));
        assertEquals("Hallo", FileUtils.removeTest("Hallo"));
        assertEquals("Hallo", FileUtils.removeTest("HalloTest"));
        assertEquals("Hallo.", FileUtils.removeTest("Hallo.Test"));
    }

    @Test
    public void testReadAllLines()
    {
        String[] lines = { "  line1   ", "line2" };
        File tempfile = new File("tempfilelines");
        FileUtils.writeAllLines(tempfile, lines);
        String[] readLines = FileUtils.readAllLines(tempfile);

        assertEquals(lines.length, readLines.length);
        for (int i = 0; i < lines.length; i++)
        {
            assertEquals(lines[i], readLines[i]);
        }

        tempfile.delete();
    }
}
