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
import org.junit.Test;

import de.bmw.carit.acme.cli.ConsoleOutput;

public class ProjectTests
{

    private static final String TESTPR_DIR = "testdir";
    private static final String TESTPR_NAME = "testpr";

    @AfterClass
    public static void after()
    {
        FileUtils.deleteDirectory(TESTPR_DIR);
    }

    @Test
    public void testCreateProject1()
    {
        ACME.createProject(TESTPR_DIR, TESTPR_NAME, new ConsoleOutput());

        File rootDir = new File(TESTPR_DIR, TESTPR_NAME);
        File modulesDir = new File(rootDir, Constants.MODULES_FOLDERNAME);
        assertTrue(rootDir.exists());
        assertTrue(modulesDir.exists());
    }

    @Test
    public void testCreateProject2()
    {
        ACME.createProject(TESTPR_DIR, "###invalidNAME###", new ConsoleOutput());

        File rootDir = new File(TESTPR_DIR, "###invalidNAME###");
        assertFalse(rootDir.exists());
    }
}
