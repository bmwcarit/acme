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
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.bmw.carit.acme.cli.ConsoleOutput;

public class FileTests
{
    private static final String TESTPR_DIR = "testdir";
    private static final String TESTPR_NAME = "testpr";
    private static final String TESTMOD_NAME = "testmodule";
    private static final String TESTFILE1_NAME = "testclass1";
    private static final String TESTFILE2_NAME = "testclass2";
    private static final String TESTFILE3_NAME = "testclass3";
    private static final String TESTFILE4_NAME = "testclass4";
    private static final String TESTFILE5_NAME = "testclass5";
    private static final String RENAMEDFILE1_NAME = "renamedClass";
    private static File projectDir;

    @BeforeClass
    public static void before()
    {
        ACME.createProject(TESTPR_DIR, TESTPR_NAME, new ConsoleOutput());
        projectDir = new File(TESTPR_DIR, TESTPR_NAME);
        ACME.createModule(projectDir.toString(), TESTMOD_NAME, ModuleType.Static, new ConsoleOutput());
    }

    @AfterClass
    public static void after()
    {
        FileUtils.deleteDirectory(TESTPR_DIR);
    }

    @Test
    public void testCreateFile1()
    {
        File modulesDir = new File(projectDir, Constants.MODULES_FOLDERNAME);
        File moduleDir = new File(modulesDir, TESTMOD_NAME);
        File src = new File(moduleDir, Constants.SRC_FOLDER_NAME);
        File publicInclude = new File(new File(moduleDir, Constants.INCLUDE_FOLDER_NAME), TESTMOD_NAME);
        File test = new File(moduleDir, Constants.TEST_FOLDER_NAME);

        ACME.createFile(moduleDir.toString(), TESTFILE1_NAME, FileType.CPP, null, true, true, true, true, new ConsoleOutput());

        assertTrue(new File(src, TESTFILE1_NAME + ".cpp").exists());
        assertTrue(new File(publicInclude, TESTFILE1_NAME + ".h").exists());
        assertTrue(new File(test, TESTFILE1_NAME + "Test.h").exists());
        assertTrue(new File(test, TESTFILE1_NAME + "Test.cpp").exists());
    }

    @Test
    public void testCreateFile2()
    {
        File modulesDir = new File(projectDir, Constants.MODULES_FOLDERNAME);
        File moduleDir = new File(modulesDir, TESTMOD_NAME);
        File src = new File(moduleDir, Constants.SRC_FOLDER_NAME);
        File privateInclude = new File(moduleDir, Constants.INCLUDE_FOLDER_NAME);
        File test = new File(moduleDir, Constants.TEST_FOLDER_NAME);

        ACME.createFile(moduleDir.toString(), TESTFILE2_NAME, FileType.CPP, null, false, true, true, true, new ConsoleOutput());

        assertTrue(new File(src, TESTFILE2_NAME + ".cpp").exists());
        assertTrue(new File(privateInclude, TESTFILE2_NAME + ".h").exists());
        assertTrue(new File(test, TESTFILE2_NAME + "Test.h").exists());
        assertTrue(new File(test, TESTFILE2_NAME + "Test.cpp").exists());
    }

    @Test
    public void testCreateFile3()
    {
        File modulesDir = new File(projectDir, Constants.MODULES_FOLDERNAME);
        File moduleDir = new File(modulesDir, TESTMOD_NAME);
        File src = new File(moduleDir, Constants.SRC_FOLDER_NAME);
        File publicInclude = new File(new File(moduleDir, Constants.INCLUDE_FOLDER_NAME), TESTMOD_NAME);
        File test = new File(moduleDir, Constants.TEST_FOLDER_NAME);

        ACME.createFile(moduleDir.toString(), TESTFILE3_NAME, FileType.CPP, null, true, false, true, true, new ConsoleOutput());

        assertTrue(new File(src, TESTFILE3_NAME + ".cpp").exists());
        assertTrue(new File(publicInclude, TESTFILE3_NAME + ".h").exists());
        assertFalse(new File(test, TESTFILE3_NAME + "Test.h").exists());
        assertFalse(new File(test, TESTFILE3_NAME + "Test.cpp").exists());
    }

    @Test
    public void testCreateFile4()
    {
        File modulesDir = new File(projectDir, Constants.MODULES_FOLDERNAME);
        File moduleDir = new File(modulesDir, TESTMOD_NAME);
        File src = new File(moduleDir, Constants.SRC_FOLDER_NAME);
        File publicInclude = new File(new File(moduleDir, Constants.INCLUDE_FOLDER_NAME), TESTMOD_NAME);
        File test = new File(moduleDir, Constants.TEST_FOLDER_NAME);

        ACME.createFile(moduleDir.toString(), TESTFILE4_NAME, FileType.CPP, null, true, false, false, true, new ConsoleOutput());

        assertTrue(new File(src, TESTFILE4_NAME + ".cpp").exists());
        assertFalse(new File(publicInclude, TESTFILE4_NAME + ".h").exists());
        assertFalse(new File(test, TESTFILE4_NAME + "Test.h").exists());
        assertFalse(new File(test, TESTFILE4_NAME + "Test.cpp").exists());
    }

    @Test
    public void testCreateFile5()
    {
        File modulesDir = new File(projectDir, Constants.MODULES_FOLDERNAME);
        File moduleDir = new File(modulesDir, TESTMOD_NAME);
        File src = new File(moduleDir, Constants.SRC_FOLDER_NAME);
        File publicInclude = new File(new File(moduleDir, Constants.INCLUDE_FOLDER_NAME), TESTMOD_NAME);
        File test = new File(moduleDir, Constants.TEST_FOLDER_NAME);

        ACME.createFile(moduleDir.toString(), TESTFILE5_NAME, FileType.CPP, null, true, true, true, false, new ConsoleOutput());

        assertFalse(new File(src, TESTFILE5_NAME + ".cpp").exists());
        assertTrue(new File(publicInclude, TESTFILE5_NAME + ".h").exists());
        assertTrue(new File(test, TESTFILE5_NAME + "Test.h").exists());
        assertTrue(new File(test, TESTFILE5_NAME + "Test.cpp").exists());
    }

    @Test
    public void testGetExtension()
    {
        String str = FileUtils.getExtension("test.exe");
        assertEquals(".exe", str);

        str = FileUtils.getExtension("test.extension");
        assertEquals(".extension", str);

        str = FileUtils.getExtension("noextension");
        assertEquals("noextension", str);

        str = FileUtils.getExtension("ext.ext.ext");
        assertEquals(".ext", str);
    }

    @Test
    public void testRemoveExtension()
    {
        String str = FileUtils.removeExtension("test.exe");
        assertEquals("test", str);

        str = FileUtils.removeExtension("test.extension");
        assertEquals("test", str);

        str = FileUtils.removeExtension("noextension");
        assertEquals("noextension", str);

        str = FileUtils.removeExtension("ext.ext.ext");
        assertEquals("ext.ext", str);
    }

    @Test
    public void testRenameFile1()
    {
        File modulesDir = new File(projectDir, Constants.MODULES_FOLDERNAME);
        File moduleDir = new File(modulesDir, TESTMOD_NAME);
        ACME.renameFile(moduleDir.toString(), TESTFILE1_NAME, null, RENAMEDFILE1_NAME, new ConsoleOutput());

        File src = new File(moduleDir, Constants.SRC_FOLDER_NAME);
        File publicInclude = new File(new File(moduleDir, Constants.INCLUDE_FOLDER_NAME), TESTMOD_NAME);
        File test = new File(moduleDir, Constants.TEST_FOLDER_NAME);

        assertTrue(new File(src, RENAMEDFILE1_NAME + ".cpp").exists());
        assertTrue(new File(publicInclude, RENAMEDFILE1_NAME + ".h").exists());
        assertTrue(new File(test, RENAMEDFILE1_NAME + "Test.h").exists());
        assertTrue(new File(test, RENAMEDFILE1_NAME + "Test.cpp").exists());

        assertFalse(new File(src, TESTFILE1_NAME + ".cpp").exists());
        assertFalse(new File(publicInclude, TESTFILE1_NAME + ".h").exists());
        assertFalse(new File(test, TESTFILE1_NAME + "Test.h").exists());
        assertFalse(new File(test, TESTFILE1_NAME + "Test.cpp").exists());
    }

    @Test
    public void testDeleteFile()
    {
        File modulesDir = new File(projectDir, Constants.MODULES_FOLDERNAME);
        File moduleDir = new File(modulesDir, TESTMOD_NAME);
        File src = new File(moduleDir, Constants.SRC_FOLDER_NAME);
        File publicInclude = new File(new File(moduleDir, Constants.INCLUDE_FOLDER_NAME), TESTMOD_NAME);
        File test = new File(moduleDir, Constants.TEST_FOLDER_NAME);

        ACME.deleteFile(moduleDir.toString(), RENAMEDFILE1_NAME, null, false, false, true, new ConsoleOutput());

        assertTrue(new File(src, RENAMEDFILE1_NAME + ".cpp").exists());
        assertTrue(new File(publicInclude, RENAMEDFILE1_NAME + ".h").exists());
        assertFalse(new File(test, RENAMEDFILE1_NAME + "Test.h").exists());
        assertFalse(new File(test, RENAMEDFILE1_NAME + "Test.cpp").exists());

        ACME.deleteFile(moduleDir.toString(), RENAMEDFILE1_NAME, null, false, true, true, new ConsoleOutput());

        assertFalse(new File(src, RENAMEDFILE1_NAME + ".cpp").exists());
        assertTrue(new File(publicInclude, RENAMEDFILE1_NAME + ".h").exists());
        assertFalse(new File(test, RENAMEDFILE1_NAME + "Test.h").exists());
        assertFalse(new File(test, RENAMEDFILE1_NAME + "Test.cpp").exists());

        ACME.deleteFile(moduleDir.toString(), RENAMEDFILE1_NAME, null, true, true, true, new ConsoleOutput());

        assertFalse(new File(src, RENAMEDFILE1_NAME + ".cpp").exists());
        assertFalse(new File(publicInclude, RENAMEDFILE1_NAME + ".h").exists());
        assertFalse(new File(test, RENAMEDFILE1_NAME + "Test.h").exists());
        assertFalse(new File(test, RENAMEDFILE1_NAME + "Test.cpp").exists());
    }
}
