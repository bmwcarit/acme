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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wraps the actions to create a module.
 */
class ACMEModule
{
    public static boolean renameModule(String directory, String moduleName, String newName, IOutput output)
    {
        output.writeLine("Renaming module...");

        File moduleDirFile = determineModuleDirectory(directory, moduleName, true, output);
        if (moduleDirFile == null)
        {
            return false;
        }
        moduleName = moduleDirFile.getName();

        File newModuleDir = new File(moduleDirFile.getParentFile(), newName);
        if (newModuleDir.exists())
        {
            output.writeLine("    New module seems to exist. Delete it first.");
            return false;
        }

        output.writeLine("Will rename module '" + moduleName + "' to '" + newName + "'...");

        // do the work
        output.writeLine("    Renaming module folder.");
        if (!moduleDirFile.renameTo(newModuleDir))
        {
            output.writeLine("    Could not rename folder. Any open files or open IDEs?");
            return false;
        }

        adjustCMakeListsForRenaming(moduleName, newName, newModuleDir, output);

        // done
        return true;
    }

    public static boolean createModule(String directory, String name, ModuleType type, IOutput output)
    {
        // initial checks
        output.writeLine("Creating module '" + name + "' with type " + type.toString() + " ...");
        if (!FileUtils.isNameOk(name))
        {
            // name has invalid characters
            output.writeLine(String.format("    The module name '%s' is invalid.", name));
            return false;
        }

        File moduleDirFile = determineModuleDirectory(directory, name, false, output);
        if (moduleDirFile == null)
        {
            return false;
        }
        name = moduleDirFile.getName();

        if (moduleDirFile.exists())
        {
            // only create it once
            output.writeLine("    Module seems to exist. Delete it first.");
            return false;
        }

        // do the work
        setupModuleStructure(moduleDirFile, output);
        createModuleSpecificCMakeLists(name, type, moduleDirFile, output);
        adjustCMakeListsForCreation(moduleDirFile.getParentFile(), name, output);
        handleModuleType(moduleDirFile, name, type, output);

        // done
        return true;
    }

    public static boolean deleteModule(String directory, String moduleName, IOutput output)
    {
        output.writeLine("Deleting module...");

        File moduleDirFile = determineModuleDirectory(directory, moduleName, true, output);
        if (moduleDirFile == null)
        {
            return false;
        }
        moduleName = moduleDirFile.getName();

        // do the work
        File modulesDir = moduleDirFile.getParentFile();
        Map<String, String> variables = new HashMap<String, String>();
        variables.put("moduleName", moduleName);
        variables.put("projectName", FileUtils.getProjectName(moduleDirFile.getAbsolutePath()));
        String add_module_command = TemplateHandling.getProperty("FileContents.properties", "ADD_SUBDIRECTORY_TEMPLATE", variables, moduleDirFile);

        // first check if we have complete write access to the directory through renaming
        File tempMod = new File(moduleDirFile.getParentFile(), "backup" + moduleName);
        if (!moduleDirFile.renameTo(tempMod))
        {
            // renaming did not work, delete will never work!
            output.writeLine("    Could not even rename module folder. Any open files or open IDEs or did you use the function from inside the folder?");
            return false;
        }

        // now delete the directory
        output.writeLine("    Deleting module folder");
        FileUtils.deleteDirectory(tempMod);

        adjustCMakeListsForDeletion(modulesDir, add_module_command, output);

        return true;
    }

    public static File determineModuleDirectory(String directory, String moduleName, boolean needsToExist, IOutput output)
    {
        File moduleDirFile = null;
        if (moduleName == null)
        {
            // try auto-detection of module directory
            moduleDirFile = FileUtils.findCurrentModuleDir(directory);
            if (moduleDirFile == null)
            {
                output.writeLine("    Current folder seems not to be inside a valid ACME module: " + directory);
                return null;
            }
        }
        else
        {
            // pre-defined module name -> determine directory name
            moduleDirFile = FileUtils.findModuleDir(directory, moduleName);
            if (moduleDirFile == null)
            {
                output.writeLine("    Current folder seems not to be inside a valid ACME project structure: " + directory);
                return null;
            }
        }
        if (needsToExist && !isModuleDirectory(moduleDirFile))
        {
            output.writeLine("    Module '" + moduleName + "' seems not to be a valid ACME module. Is CMakeLists.txt present?");
            return null;
        }
        moduleName = moduleDirFile.getName();
        output.writeLine("    Recognized module '" + moduleName + "'");
        return moduleDirFile;
    }

    public static boolean isModuleDirectory(File moduleDir)
    {
        return moduleDir.exists() && moduleDir.isDirectory() && new File(moduleDir, Constants.CMAKELISTS_FILENAME).exists();
    }

    private static void adjustCMakeListsForRenaming(String name, String newName, File newModuleDir, IOutput output)
    {
        // change the modules CMakeLists
        output.writeLine("    Changing entry in CMakeLists.");
        File cmakeLists = new File(newModuleDir.getParentFile(), Constants.CMAKELISTS_FILENAME);
        String[] lines = FileUtils.readAllLines(cmakeLists);
        Map<String, String> variables = new HashMap<String, String>();
        variables.put("moduleName", name);
        variables.put("projectName", FileUtils.getProjectName(newModuleDir.getAbsolutePath()));
        String seachString = TemplateHandling.getProperty("FileContents.properties", "ADD_SUBDIRECTORY_TEMPLATE", variables, newModuleDir); // old name

        variables.clear();
        variables.put("moduleName", newName);
        variables.put("projectName", FileUtils.getProjectName(newModuleDir.getAbsolutePath()));
        String addSubdirectoryCommand = TemplateHandling.getProperty("FileContents.properties", "ADD_SUBDIRECTORY_TEMPLATE", variables, newModuleDir);

        boolean found = false;
        for (int i = 0; i < lines.length; i++)
        {
            if (lines[i].trim().equalsIgnoreCase(seachString))
            {
                // line found
                lines[i] = addSubdirectoryCommand;
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
            output.writeLine("    Could not change text in the 'modules' CMakeLists file. It remains unchanged.");
        }

        // change the module cmake lists itself
        found = false;
        output.writeLine("    Changing entry in module-specific CMakeLists.");
        cmakeLists = new File(newModuleDir, Constants.CMAKELISTS_FILENAME);
        lines = FileUtils.readAllLines(cmakeLists);
        variables.clear();
        variables.put("moduleName", name);
        variables.put("projectName", FileUtils.getProjectName(newModuleDir.getAbsolutePath()));
        seachString = TemplateHandling.getProperty("FileContents.properties", "ADD_MODULE_PREFIX_TEMPLATE", variables, newModuleDir); // old name

        for (int i = 0; i < lines.length; i++)
        {
            if (lines[i].trim().toLowerCase().startsWith(seachString.toLowerCase()))
            {
                // line found
                String addModuleCommand = lines[i].replace(name, newName);
                lines[i] = addModuleCommand;
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
            output.writeLine("    Could not change text in the module-specific CMakeLists file. It remains unchanged.");
        }
    }

    private static void adjustCMakeListsForCreation(File modulesDirName, String name, IOutput output)
    {
        output.writeLine("    Registering module in project");
        File filename = new File(modulesDirName, Constants.CMAKELISTS_FILENAME);
        if (!filename.exists())
        {
            try
            {
                filename.createNewFile();
            }
            catch (IOException e)
            {
                throw new IllegalStateException(e);
            }
        }

        Map<String, String> variables = new HashMap<String, String>();
        variables.put("moduleName", name);
        variables.put("projectName", FileUtils.getProjectName(modulesDirName.getAbsolutePath()));
        String subDirCommand = TemplateHandling.getProperty("FileContents.properties", "ADD_SUBDIRECTORY_TEMPLATE", variables, modulesDirName);

        List<String> lines = new ArrayList<String>(Arrays.asList(FileUtils.readAllLines(filename)));
        lines.add(subDirCommand);
        FileUtils.writeAllLines(filename, lines.toArray(new String[lines.size()]));
    }

    private static void adjustCMakeListsForDeletion(File modulesDir, String add_module_command, IOutput output)
    {
        output.writeLine("    Removing entry in CMakeLists");
        File cmakelists = new File(modulesDir, Constants.CMAKELISTS_FILENAME);
        List<String> strs = new ArrayList<String>(Arrays.asList(FileUtils.readAllLines(cmakelists)));
        for (int i = 0; i < strs.size(); i++)
        {
            if (strs.get(i).trim().contains(add_module_command))
            {
                strs.remove(i);
            }
        }
        String[] lines = strs.toArray(new String[strs.size()]);
        FileUtils.writeAllLines(cmakelists, lines);
    }

    private static void createModuleSpecificCMakeLists(String name, ModuleType type, File moduleDirName, IOutput output)
    {
        output.writeLine("    Creating module-specific CMakeLists");
        Map<String, String> variables = new HashMap<String, String>();
        variables.put("moduleName", name);
        variables.put("projectName", FileUtils.getProjectName(moduleDirName.getAbsolutePath()));
        variables.put("moduleType", type.toString().toLowerCase());
        String moduleContent = TemplateHandling.getContent("Module.template", variables, moduleDirName);
        FileUtils.writeAllText(new File(moduleDirName, Constants.CMAKELISTS_FILENAME), moduleContent);
    }

    private static void handleModuleType(File moduleDirName, String moduleName, ModuleType type, IOutput output)
    {
        if (type == ModuleType.Exe)
        {
            // an 'exe' module always gets a 'main.cpp'
            output.writeLine("    Creating main file");
            Map<String, String> variables = new HashMap<String, String>();
            variables.put("moduleName", moduleName);
            variables.put("projectName", FileUtils.getProjectName(moduleDirName.getAbsolutePath()));
            String content = TemplateHandling.getContent("main.cpp.template", variables, moduleDirName);
            File filePath = FileUtils.concatenatePath(moduleDirName, Constants.SRC_FOLDER_NAME, "main.cpp");
            filePath.getParentFile().mkdirs();
            FileUtils.writeAllText(filePath, content);
            FileUtils.adjustModuleCMakeLists(moduleDirName, "main", true, output);
        }

        // later we might have more type specific things
    }

    private static void setupModuleStructure(File moduleDirName, IOutput output)
    {
        output.writeLine("    Setup module structure");
        moduleDirName.mkdirs();
        new File(moduleDirName, Constants.SRC_FOLDER_NAME).mkdirs();
        new File(moduleDirName, Constants.INCLUDE_FOLDER_NAME).mkdirs();
        new File(moduleDirName, Constants.TEST_FOLDER_NAME).mkdirs();
    }
}
