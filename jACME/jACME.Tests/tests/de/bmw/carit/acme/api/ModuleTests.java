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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.bmw.carit.acme.cli.ConsoleOutput;

public class ModuleTests
{
    private static final String TESTPR_DIR = "testdir";
    private static final String TESTPR_NAME = "testpr";
    private static final String TESTMOD1_NAME = "testmodule";
    private static final String TESTMOD2_NAME = "testexemodule";
    private static final String RENAMEDMOD_NAME = "testrename";

    @BeforeClass
    public static void before()
    {
        ACME.createProject(TESTPR_DIR, TESTPR_NAME, new ConsoleOutput());
    }

    @AfterClass
    public static void after()
    {
        FileUtils.deleteDirectory(TESTPR_DIR);
    }

    @Test
    public void testCreateModule()
    {
        File rootDir = new File(TESTPR_DIR, TESTPR_NAME);

        ACME.createModule(rootDir.toString(), TESTMOD1_NAME, ModuleType.Static, new ConsoleOutput());

        File modulesDir = new File(rootDir, Constants.MODULES_FOLDERNAME);
        File moduleDir = new File(modulesDir, TESTMOD1_NAME);
        File src = new File(moduleDir, Constants.SRC_FOLDER_NAME);
        File include = new File(moduleDir, Constants.INCLUDE_FOLDER_NAME);
        File test = new File(moduleDir, Constants.TEST_FOLDER_NAME);
        assertTrue(rootDir.exists());
        assertTrue(modulesDir.exists());
        assertTrue(moduleDir.exists());
        assertTrue(src.exists());
        assertTrue(include.exists());
        assertTrue(test.exists());
    }

    @Test
    public void testCreateExeModule()
    {
        File rootDir = new File(TESTPR_DIR, TESTPR_NAME);

        ACME.createModule(rootDir.toString(), TESTMOD2_NAME, ModuleType.Exe, new ConsoleOutput());

        File modulesDir = new File(rootDir, Constants.MODULES_FOLDERNAME);
        File moduleDir = new File(modulesDir, TESTMOD2_NAME);
        File src = new File(moduleDir, Constants.SRC_FOLDER_NAME);
        File mainfile = new File(src, "main.cpp");
        File include = new File(moduleDir, Constants.INCLUDE_FOLDER_NAME);
        File test = new File(moduleDir, Constants.TEST_FOLDER_NAME);
        assertTrue(rootDir.exists());
        assertTrue(modulesDir.exists());
        assertTrue(moduleDir.exists());
        assertTrue(src.exists());
        assertTrue(include.exists());
        assertTrue(test.exists());
        assertTrue(mainfile.exists());
    }

    @Test
    public void testRenameModule()
    {
        File rootDir = new File(TESTPR_DIR, TESTPR_NAME);
        File modulesDir = new File(rootDir, Constants.MODULES_FOLDERNAME);
        File moduleDir = new File(modulesDir, TESTMOD2_NAME);
        File newModuleDir = new File(modulesDir, RENAMEDMOD_NAME);

        ACME.renameModule(moduleDir.toString(), TESTMOD2_NAME, RENAMEDMOD_NAME, new ConsoleOutput());

        assertTrue(newModuleDir.exists());
        assertFalse(moduleDir.exists());

        ACME.renameModule(moduleDir.toString(), RENAMEDMOD_NAME, TESTMOD2_NAME, new ConsoleOutput());

        assertFalse(newModuleDir.exists());
        assertTrue(moduleDir.exists());
    }

    @Test
    public void testUnusedFiles()
    {
        File rootDir = new File(TESTPR_DIR, TESTPR_NAME);
        ACME.printUnusedFiles(rootDir.getAbsolutePath(), TESTMOD2_NAME, true, new ConsoleOutput());
    }

    @Test
    public void testDeleteModule()
    {
        File rootDir = new File(TESTPR_DIR, TESTPR_NAME);

        ACME.deleteModule(rootDir.getAbsolutePath(), TESTMOD2_NAME, new ConsoleOutput());
    }
}
