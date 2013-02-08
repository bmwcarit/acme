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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wraps the actions to create a file.
 */
class ACMEFile
{
    public static boolean deleteFile(String directory, String name, String moduleName, boolean header, boolean source, boolean test, IOutput output)
    {
        // initial checks
        output.writeLine("Deleting file '" + name + "' ...");
        name = FileUtils.removeExtension(name);
        name = FileUtils.removeTest(name);
        File moduleDirFile = ACMEModule.determineModuleDirectory(directory, moduleName, true, output);
        if (moduleDirFile == null)
        {
            return false;
        }
        moduleName = moduleDirFile.getName();

        // do the work
        if (deleteFilesInFilesystem(name, moduleName, header, source, test, moduleDirFile, output))
        {
            // only adjust cmake lists if no files are left
            adjustCMakeListsForDeletion(name, moduleDirFile, output);
        }

        // success
        return true;
    }

    private static boolean deleteFilesInFilesystem(String name, String moduleName, boolean header, boolean source, boolean test, File moduleDirFile, IOutput output)
    {
        output.writeLine("    Deleting files in filesystem");
        File headerFile = new File(new File(moduleDirFile, Constants.INCLUDE_FOLDER_NAME), name + ".h");
        if (!headerFile.exists())
        {
            headerFile = new File(new File(new File(moduleDirFile, Constants.INCLUDE_FOLDER_NAME), moduleName), name + ".h");
        }
        File sourceFile = null;
        for (FileType type : FileType.values())
        {
            sourceFile = FileUtils.concatenatePath(moduleDirFile, Constants.SRC_FOLDER_NAME, name + type.getSourceFileExtension());
            if (sourceFile.exists())
            {
                break;
            }
        }
        File testSrcFile = new File(new File(moduleDirFile, Constants.TEST_FOLDER_NAME), name + "Test.cpp");
        File testHeaderFile = new File(new File(moduleDirFile, Constants.TEST_FOLDER_NAME), name + "Test.h");

        if (header && headerFile.exists())
        {
            headerFile.delete();
        }
        if (source && sourceFile != null && sourceFile.exists())
        {
            sourceFile.delete();
        }
        if (test)
        {
            testSrcFile.delete();
            testHeaderFile.delete();
        }

        // check if any files are left in file system
        boolean anyFilesLeft = headerFile.exists() || sourceFile.exists() || testSrcFile.exists() || testHeaderFile.exists();
        return !anyFilesLeft;
    }

    private static void adjustCMakeListsForDeletion(String name, File moduleDirFile, IOutput output)
    {
        Map<String, String> variables = new HashMap<String, String>();
        variables.put("fileName", name);
        variables.put("moduleName", moduleDirFile.getName());
        variables.put("projectName", FileUtils.getProjectName(moduleDirFile.getAbsolutePath()));
        String add_file_command = TemplateHandling.getProperty("FileContents.properties", "ADD_FILE_TEMPLATE", variables, moduleDirFile);

        output.writeLine("    Adjusting CMakeLists");
        File cmakelists = new File(moduleDirFile, Constants.CMAKELISTS_FILENAME);
        List<String> strs = new ArrayList<String>(Arrays.asList(FileUtils.readAllLines(cmakelists)));
        boolean found = false;
        for (int i = 0; i < strs.size(); i++)
        {
            if (strs.get(i).trim().contains(add_file_command))
            {
                strs.remove(i);
                found = true;
                break;
            }
        }
        if (found)
        {
            String[] lines = strs.toArray(new String[strs.size()]);
            FileUtils.writeAllLines(cmakelists, lines);
        }
        else
        {
            output.writeLine("    Could not remove text in the CMakeLists file. It remains unchanged.");
        }
    }

    public static boolean renameFile(String directory, String name, String moduleName, String newName, IOutput output)
    {
        // initial checks
        output.writeLine("Renaming file '" + name + "' to '" + newName + "'...");
        name = FileUtils.removeExtension(name);
        name = FileUtils.removeTest(name);

        File moduleDirFile = ACMEModule.determineModuleDirectory(directory, moduleName, true, output);
        if (moduleDirFile == null)
        {
            return false;
        }
        moduleName = moduleDirFile.getName();

        // prepare the files and do existence check

        // 1. public header
        File headerFile = FileUtils.concatenatePath(moduleDirFile, Constants.INCLUDE_FOLDER_NAME, moduleName, name + ".h");

        // 2. private header
        if (!headerFile.exists())
        {
            headerFile = FileUtils.concatenatePath(moduleDirFile, Constants.INCLUDE_FOLDER_NAME, name + ".h");
        }

        // 3. source
        File sourceFile = null;
        for (FileType type : FileType.values())
        {
            sourceFile = FileUtils.concatenatePath(moduleDirFile, Constants.SRC_FOLDER_NAME, name + type.getSourceFileExtension());
            if (sourceFile.exists())
            {
                break;
            }
        }

        // 4. tests
        File testSourceFile = FileUtils.concatenatePath(moduleDirFile, Constants.TEST_FOLDER_NAME, name + "Test.cpp");
        File testHeaderFile = FileUtils.concatenatePath(moduleDirFile, Constants.TEST_FOLDER_NAME, name + "Test.h");

        // the headers need a special test
        File newPublicHeaderFile = FileUtils.concatenatePath(moduleDirFile, Constants.INCLUDE_FOLDER_NAME, moduleName, newName + ".h");
        File newPrivateHeaderFile = FileUtils.concatenatePath(moduleDirFile, Constants.INCLUDE_FOLDER_NAME, newName + ".h");
        if (newPublicHeaderFile.exists() || newPrivateHeaderFile.exists())
        {
            output.writeLine("    New file seems to exist. Delete it first.");
            return false;
        }

        // new files
        File newHeaderFile = new File(headerFile.getParentFile(), newName + ".h");
        File newSourceFile = new File("nonexisting");
        if (sourceFile != null)
        {
            newSourceFile = new File(sourceFile.getParentFile(), newName + FileUtils.getExtension(sourceFile.getName()));
        }
        File newTestSourceFile = new File(testSourceFile.getParentFile(), newName + "Test.cpp");
        File newTestHeaderFile = new File(testHeaderFile.getParentFile(), newName + "Test.h");

        if (newHeaderFile.exists() || newSourceFile.exists() || newTestSourceFile.exists() || newTestHeaderFile.exists())
        {
            output.writeLine("    New file seems to exist. Delete it first.");
            return false;
        }
        if (!headerFile.exists() && !sourceFile.exists() && !testSourceFile.exists() && !testHeaderFile.exists())
        {
            output.writeLine("    Specified file was not found.");
            return false;
        }

        // do the work
        if (headerFile.exists())
        {
            output.writeLine("    Renaming header.");
            headerFile.renameTo(newHeaderFile);
        }
        if (sourceFile.exists())
        {
            output.writeLine("    Renaming source.");
            sourceFile.renameTo(newSourceFile);
        }
        if (testSourceFile.exists())
        {
            output.writeLine("    Renaming test source.");
            testSourceFile.renameTo(newTestSourceFile);
        }
        if (testHeaderFile.exists())
        {
            output.writeLine("    Renaming test header.");
            testHeaderFile.renameTo(newTestHeaderFile);
        }

        adjustCMakeListsForRenaming(name, newName, moduleDirFile, output);

        return true;
    }

    public static boolean createFile(String directory, String name, FileType type, String moduleName, boolean isPublic, boolean withTests, boolean withHeader, boolean withSource, IOutput output)
    {
        // initial checks
        output.writeLine("Creating file '" + name + "'...");
        if (!FileUtils.isNameOk(name))
        {
            // name has invalid characters
            output.writeLine(String.format("    The filename '%s' is invalid.", name));
            return false;
        }
        name = FileUtils.removeExtension(name);

        File moduleDirFile = ACMEModule.determineModuleDirectory(directory, moduleName, true, output);
        if (moduleDirFile == null)
        {
            return false;
        }
        moduleName = moduleDirFile.getName();

        // do the work
        boolean creationHappend = false;
        if (withHeader)
        {
            if (headerExists(moduleDirFile, moduleName, name))
            {
                output.writeLine("    Skipping header because it seems to exist!");
            }
            else
            {
                writeHeader(moduleDirFile, name, moduleName, isPublic, type, output);
                creationHappend = true;
            }
        }
        isPublic = isPublicHeader(moduleDirFile, moduleName, name); // check if the header is public (maybe wrong if file already exists or was skipped by user)

        if (withSource)
        {
            if (sourceExists(moduleDirFile, moduleName, name, type))
            {
                output.writeLine("    Skipping source because it seems to exist!");
            }
            else
            {
                writeSource(moduleDirFile, name, moduleName, isPublic, type, output);
                creationHappend = true;
            }
        }
        if (withTests)
        {
            if (testsExists(moduleDirFile, moduleName, name, type))
            {
                output.writeLine("    Skipping tests because they seem to exist!");
            }
            else
            {
                writeTestHeader(moduleDirFile, name, moduleName, isPublic, output);
                writeTestSource(moduleDirFile, name, moduleName, isPublic, output);
                creationHappend = true;
            }
        }

        // finally, adjust CMakeLists.txt
        FileUtils.adjustModuleCMakeLists(moduleDirFile, name, creationHappend, output);

        // done
        return true;
    }

    private static void adjustCMakeListsForRenaming(String name, String newName, File moduleDirFile, IOutput output)
    {
        output.writeLine("    Adjusting CMakeLists.");
        File cmakeLists = new File(moduleDirFile, Constants.CMAKELISTS_FILENAME);
        String[] lines = FileUtils.readAllLines(cmakeLists);
        Map<String, String> variables = new HashMap<String, String>();
        variables.put("fileName", name);
        variables.put("moduleName", moduleDirFile.getName());
        variables.put("projectName", FileUtils.getProjectName(moduleDirFile.getAbsolutePath()));
        String seachString = TemplateHandling.getProperty("FileContents.properties", "ADD_FILE_TEMPLATE", variables, moduleDirFile); // old name

        variables.clear();
        variables.put("fileName", newName);
        variables.put("moduleName", moduleDirFile.getName());
        variables.put("projectName", FileUtils.getProjectName(moduleDirFile.getAbsolutePath()));
        String addFileCommand = TemplateHandling.getProperty("FileContents.properties", "ADD_FILE_TEMPLATE", variables, moduleDirFile);

        boolean found = false;
        for (int i = 0; i < lines.length; i++)
        {
            if (lines[i].trim().equalsIgnoreCase(seachString))
            {
                // line found
                lines[i] = addFileCommand;
                found = true;
                break;
            }
        }

        if (found)
        {
            FileUtils.writeAllLines(cmakeLists, lines);
        }
        else
        {
            output.writeLine("    Could not insert text in the CMakeLists file. It remains unchanged.");
        }
    }

    private static boolean testsExists(File moduleDirName, String moduleName, String name, FileType type)
    {
        // test source file
        File filePath = FileUtils.concatenatePath(moduleDirName, Constants.TEST_FOLDER_NAME, name + "Test.cpp");

        // if one of these three exists, we assume that the file exists
        return filePath.exists();
    }

    private static boolean sourceExists(File moduleDirName, String moduleName, String name, FileType type)
    {
        // source file cpp
        File filePath = FileUtils.concatenatePath(moduleDirName, Constants.SRC_FOLDER_NAME, name + type.getSourceFileExtension());

        // if one of these three exists, we assume that the file exists
        return filePath.exists();
    }

    private static boolean isPublicHeader(File moduleDirFile, String moduleName, String name)
    {
        // public header path
        File filePath1 = FileUtils.concatenatePath(moduleDirFile, Constants.INCLUDE_FOLDER_NAME, moduleName, name + ".h");
        return filePath1.exists();
    }

    private static boolean headerExists(File moduleDirName, String moduleName, String name)
    {
        // public header
        File filePath1 = FileUtils.concatenatePath(moduleDirName, Constants.INCLUDE_FOLDER_NAME, moduleName, name + ".h");

        // private header
        File filePath2 = FileUtils.concatenatePath(moduleDirName, Constants.INCLUDE_FOLDER_NAME, name + ".h");

        // if one of these three exists, we assume that the file exists
        return filePath1.exists() || filePath2.exists();
    }

    private static void writeTestSource(File moduleDirName, String name, String moduleName, boolean isPublic, IOutput output)
    {
        output.writeLine("    Creating test source");
        Map<String, String> variables = new HashMap<String, String>();
        String includePath = name + ".h";
        if (isPublic)
        {
            includePath = moduleName + "/" + includePath;
        }
        variables.put("includePath", includePath);
        variables.put("fileName", name);
        variables.put("moduleName", moduleName);
        variables.put("projectName", FileUtils.getProjectName(moduleDirName.getAbsolutePath()));
        String content = TemplateHandling.getContent("Test.cpp.template", variables, moduleDirName);
        File filePath = FileUtils.concatenatePath(moduleDirName, Constants.TEST_FOLDER_NAME, name + "Test.cpp");
        filePath.getParentFile().mkdirs();
        FileUtils.writeAllText(filePath, content);
    }

    private static void writeTestHeader(File moduleDirName, String name, String moduleName, boolean isPublic, IOutput output)
    {
        output.writeLine("    Creating test header");
        Map<String, String> variables = new HashMap<String, String>();
        String includePath = name + ".h";
        if (isPublic)
        {
            includePath = moduleName + "/" + includePath;
        }
        variables.put("includePath", includePath);
        variables.put("fileName", name);
        variables.put("moduleName", moduleName);
        variables.put("projectName", FileUtils.getProjectName(moduleDirName.getAbsolutePath()));
        String content = TemplateHandling.getContent("Test.h.template", variables, moduleDirName);
        File filePath = FileUtils.concatenatePath(moduleDirName, Constants.TEST_FOLDER_NAME, name + "Test.h");
        filePath.getParentFile().mkdirs();
        FileUtils.writeAllText(filePath, content);
    }

    private static void writeSource(File moduleDirName, String name, String moduleName, boolean isPublic, FileType type, IOutput output)
    {
        output.writeLine("    Creating source file");
        Map<String, String> variables = new HashMap<String, String>();
        String includePath = name + ".h";
        if (isPublic)
        {
            includePath = moduleName + "/" + includePath;
        }
        variables.put("includePath", includePath);
        variables.put("fileName", name);
        variables.put("moduleName", moduleName);
        variables.put("projectName", FileUtils.getProjectName(moduleDirName.getAbsolutePath()));
        String content = TemplateHandling.getContent(type.getSourceTemplateName(), variables, moduleDirName);
        File filePath = FileUtils.concatenatePath(moduleDirName, Constants.SRC_FOLDER_NAME, name + type.getSourceFileExtension());
        filePath.getParentFile().mkdirs();
        FileUtils.writeAllText(filePath, content);
    }

    private static void writeHeader(File moduleDirName, String name, String moduleName, boolean isPublic, FileType type, IOutput output)
    {
        output.writeLine("    Creating header file");
        Map<String, String> variables = new HashMap<String, String>();
        String includePath = name + ".h";
        if (isPublic)
        {
            includePath = moduleName + "/" + includePath;
        }
        variables.put("includePath", includePath);
        variables.put("fileName", name);
        variables.put("moduleName", moduleName);
        variables.put("projectName", FileUtils.getProjectName(moduleDirName.getAbsolutePath()));
        String content = TemplateHandling.getContent(type.getHeaderTemplateName(), variables, moduleDirName);
        String moduleFolderName = isPublic ? moduleName : "";
        File filePath = FileUtils.concatenatePath(moduleDirName, Constants.INCLUDE_FOLDER_NAME, moduleFolderName, name + ".h");
        filePath.getParentFile().mkdirs();
        FileUtils.writeAllText(filePath, content);
    }
}
