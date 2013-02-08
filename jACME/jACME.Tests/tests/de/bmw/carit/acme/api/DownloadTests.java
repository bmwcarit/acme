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

public class DownloadTests
{
    private static final File TEMP_TEMPLATES = new File("tmp_templates");

    @BeforeClass
    public static void createTemplates()
    {
        FileUtils.copyDirectory(TemplateHandling.getTemplatesFolder(), TEMP_TEMPLATES);
    }

    @AfterClass
    public static void deleteTemplates()
    {
        FileUtils.deleteDirectory(TEMP_TEMPLATES);
    }

    @Test
    public void testList3psw()
    {
        ACMEDownload.list3psw(Constants.DEFAULT_3PSW_PATH, new ConsoleOutput());
    }

    @Test
    public void testDownload3psw()
    {
        String TESTPR_DIR = "testDownload";
        String TESTPR_NAME = "someproject";
        File mainDir = new File(TESTPR_DIR);
        File testDir = new File(mainDir, TESTPR_NAME);
        ACME.createProject(mainDir.getAbsolutePath(), TESTPR_NAME, new ConsoleOutput());
        assertTrue(testDir.exists());

        ACMEDownload.list3psw(Constants.DEFAULT_3PSW_PATH, new ConsoleOutput());
        ACMEDownload.download3psw(testDir.getAbsolutePath(), Constants.DEFAULT_3PSW_PATH, "Capu", new ConsoleOutput());

        FileUtils.deleteDirectory(mainDir);
    }

    @Test
    public void testDownloadForce()
    {
        File backupDir = new File(TemplateHandling.getTemplatesFolder().getParentFile(), Constants.TEMPLATES_BACKUP_FOLDER_NAME);
        FileUtils.deleteDirectory(backupDir);
        ACME.downloadTemplates(TEMP_TEMPLATES.toString(), true, false, new ConsoleOutput());
        assertTrue(backupDir.exists());
        FileUtils.deleteDirectory(backupDir);
    }

    @Test
    public void testDownloadNoForce()
    {
        File backupDir = new File(TemplateHandling.getTemplatesFolder().getParentFile(), Constants.TEMPLATES_BACKUP_FOLDER_NAME);
        FileUtils.deleteDirectory(backupDir);
        ACME.downloadTemplates(TEMP_TEMPLATES.toString(), false, false, new ConsoleOutput());
        assertFalse(backupDir.exists()); // because we copy the templates in BeforeClass, no update will be performed
    }
}
