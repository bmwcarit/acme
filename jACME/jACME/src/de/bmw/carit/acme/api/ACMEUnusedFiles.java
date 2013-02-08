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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ACMEUnusedFiles
{
    public static boolean printUnusedFiles(String directory, String moduleName, boolean printCommand, IOutput output)
    {
        output.writeLine("Finding unused files...");

        File moduleDirFile = ACMEModule.determineModuleDirectory(directory, moduleName, false, output);
        if (moduleDirFile == null)
        {
            File projectFile = FileUtils.getProjectFile(directory);
            if (projectFile == null)
            {
                return false;
            }

            // scan all modules
            List<File> allModules = getAllModules(projectFile);
            for (File moduleDir : allModules)
            {
                scanSingleModule(printCommand, output, moduleDir);
            }
        }
        else
        {
            scanSingleModule(printCommand, output, moduleDirFile);
        }

        return true;
    }

    private static List<File> getAllModules(File projectFile)
    {
        List<File> files = new ArrayList<File>();
        File modulesDir = new File(projectFile, Constants.MODULES_FOLDERNAME);
        File[] allModules = modulesDir.listFiles();
        if (allModules != null)
        {
            for (File moduleDir : allModules)
            {
                if (moduleDir.isDirectory())
                {
                    files.add(moduleDir);
                }
            }
        }

        return files;
    }

    private static void scanSingleModule(boolean printCommand, IOutput output, File moduleDirFile)
    {
        output.writeLine("    Scanning folder " + moduleDirFile.getName());
        File cmakeListsFile = new File(moduleDirFile, Constants.CMAKELISTS_FILENAME);
        if (!cmakeListsFile.exists())
        {
            output.writeLine("        No CMakeLists directory: " + moduleDirFile);
            return;
        }
        List<String> usedFiles = extractUsedFiles(cmakeListsFile);
        List<File> allFiles = getAllFiles(moduleDirFile);
        int unusedFileCount = 0;
        for (File file : allFiles)
        {
            if (!isInUsedFiles(file, usedFiles))
            {
                unusedFileCount++;
                if (printCommand)
                {
                    // may print a file twice because source and header files are printed out
                    String prefix = TemplateHandling.getProperty("FileContents.properties", "ADD_FILE_PREFIX_TEMPLATE", new HashMap<String, String>(), moduleDirFile);
                    output.writeLine("        " + prefix + "" + FileUtils.removeExtension(file.getName()) + ")");
                }
                else
                {
                    String fileString = file.toString();
                    String folderPrefix = moduleDirFile.toString();
                    fileString = fileString.replace(folderPrefix, "");
                    output.writeLine("        Unused file: " + fileString);
                }
            }
        }

        if (unusedFileCount == 0)
        {
            output.writeLine("    No unused files found...");
        }
        else
        {
            output.writeLine("    Found " + unusedFileCount + " unused file(s) in folder " + moduleDirFile.getName());
        }
    }

    private static boolean isInUsedFiles(File file, List<String> usedFiles)
    {
        String currFileName = FileUtils.removeExtension(file.getName());
        return usedFiles.contains(currFileName);
    }

    private static List<File> getAllFiles(File moduleDirFile)
    {
        ArrayList<File> list = new ArrayList<>();
        getAllFiles(new File(moduleDirFile, Constants.INCLUDE_FOLDER_NAME), list);
        getAllFiles(new File(moduleDirFile, Constants.SRC_FOLDER_NAME), list);
        return list;
    }

    private static void getAllFiles(File directory, ArrayList<File> list)
    {
        File[] files = directory.listFiles();
        if (files != null)
        {
            for (File file : files)
            {
                if (file.isDirectory())
                {
                    getAllFiles(file, list);
                }
                else
                {
                    list.add(file);
                }
            }
        }
    }

    private static List<String> extractUsedFiles(File cmakeListsFile)
    {
        ArrayList<String> list = new ArrayList<>();
        String prefix = TemplateHandling.getProperty("FileContents.properties", "ADD_FILE_PREFIX_TEMPLATE", new HashMap<String, String>(), cmakeListsFile);
        String[] cmakelistsContent = FileUtils.readAllLines(cmakeListsFile);
        for (String line : cmakelistsContent)
        {
            if (line.startsWith(prefix))
            {
                line = line.replace(prefix, "");
                int stop = line.indexOf(")");
                line = line.substring(0, stop);
                line = line.replace(")", "");
                line = line.trim();
                list.add(line);
            }
        }
        return list;
    }
}
