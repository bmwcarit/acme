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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Wraps the download action.
 */
public class ACMEDownload
{
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd.MM.yyyy, HH:mm:ss");

    public static void list3psw(String path, IOutput output)
    {
        output.writeLine("Listing available third party software...");
        File downloadPath = new File(path);

        File[] files = downloadPath.listFiles();
        if (files == null || files.length == 0)
        {
            output.writeLine("    No software found.");
            return;
        }

        for (File file : files)
        {
            Date date = new Date(getLastModified(file, false));
            output.writeLine("    " + FileUtils.removeExtension(file.getName()) + " (" + DATE_FORMATTER.format(date) + ")");
        }
    }

    public static void download3psw(String directory, String remotePath, String name, IOutput output)
    {
        output.writeLine("Downloading third party software '" + name + "'...");
        File downloadPath = new File(remotePath);
        File softwarePath = new File(downloadPath, name);
        if (!softwarePath.exists())
        {
            // try to add zip
            softwarePath = new File(downloadPath, name + ".zip");
        }

        if (!softwarePath.exists())
        {
            output.writeLine("    Specified software does not exists. Was it listed? (" + softwarePath.getAbsolutePath() + ")");
            return;
        }
        File projectDir = FileUtils.getProjectFile(directory);
        if (projectDir == null)
        {
            output.writeLine("    Current folder seems not to be inside a valid ACME project structure: " + directory);
            return;
        }

        output.writeLine("    Unzipping '" + name + "' from " + softwarePath.getAbsolutePath());
        FileUtils.unzipContent(softwarePath, projectDir);
    }

    public static void downloadTemplates(String path, boolean force, boolean check, IOutput output)
    {
        output.writeLine("Downloading new templates...");

        File templatePath = TemplateHandling.getTemplatesFolder();
        File downloadPath = new File(path);
        output.writeLine("    Using path '" + downloadPath.getAbsolutePath() + "' for download");

        if (!downloadPath.exists())
        {
            output.writeLine("    Path does not seem to exist!");
            return;
        }

        boolean updateNecessary = updateNecessary(templatePath, downloadPath);
        Date lastRemoteUpdateTime = new Date(getLastModified(downloadPath));
        Date lastLocalUpdateTime = new Date(getLastModified(templatePath));
        output.writeLine("    Remote Templates are from " + DATE_FORMATTER.format(lastRemoteUpdateTime));
        output.writeLine("    Local  Templates are from " + DATE_FORMATTER.format(lastLocalUpdateTime));

        if (check)
        {
            if (updateNecessary)
            {
                output.writeLine("    Would start download because remote templates are newer");
            }
            else
            {
                output.writeLine("    Would NOT start download because remote templates are newer");
            }
            return;
        }

        if (!force && !updateNecessary)
        {
            output.writeLine("    Everything is up to date. Use '-force' to update anyway.");
            return;
        }
        else if (force)
        {
            output.writeLine("    Forcing update");
        }
        else
        {
            output.writeLine("    Starting download because remote templates are newer");
        }

        // do the work
        File backup = new File(templatePath.getParentFile(), Constants.TEMPLATES_BACKUP_FOLDER_NAME);
        output.writeLine("    Backup of templates in " + backup.getAbsolutePath());
        FileUtils.deleteDirectory(backup);
        FileUtils.copyDirectory(templatePath, backup);
        output.writeLine("    Removing current templates...");
        FileUtils.deleteDirectory(templatePath);
        output.writeLine("    Retrieving new templates...");
        FileUtils.copyDirectory(downloadPath, templatePath);
    }

    private static boolean updateNecessary(File templatePath, File downloadPath)
    {
        long lastLocalModify = getLastModified(templatePath);
        long lastRemoteModify = getLastModified(downloadPath);
        return lastRemoteModify > lastLocalModify;
    }

    private static long getLastModified(File directory)
    {
        return getLastModified(directory, false);
    }

    private static long getLastModified(File directory, boolean recursive)
    {
        if (directory.isFile())
        {
            return directory.lastModified();
        }
        long lastModified = 0;
        File[] files = directory.listFiles();
        if (files != null)
        {
            for (File file : files)
            {
                long current = file.lastModified();
                if (current > lastModified)
                {
                    lastModified = current;
                }

                // go into directories
                if (recursive && file.isDirectory())
                {
                    current = getLastModified(file, recursive);
                    if (current > lastModified)
                    {
                        lastModified = current;
                    }
                }
            }
        }
        return lastModified;
    }
}
