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

import java.util.HashMap;
import java.util.Map;

/**
 * Public jACME-API.
 */
public class ACME
{
    /**
     * Creates a new project under the given directory.
     * 
     * @param directory
     *            The directory in which the project should get created.
     * @param name
     *            The name of the project. Must be a valid folder name.
     * @param output
     *            Output interface.
     */
    public static void createProject(String directory, String name, IOutput output)
    {
        ACMEProject.createProject(directory, name, output);
    }

    /**
     * Creates a new module in the project which is defined through the given directory.
     * 
     * @param directory
     *            The current directory. This directory must be inside a valid ACME project.
     * @param name
     *            The name of the module.
     * @param type
     *            The type of the module.
     * @param output
     *            Output interface.
     */
    public static void createModule(String directory, String name, ModuleType type, IOutput output)
    {
        ACMEModule.createModule(directory, name, type, output);
    }

    /**
     * Renames a module.
     * 
     * @param directory
     *            The current directory. This directory must be inside a valid ACME module (unless moduleName is != null, then it must only be inside a valid ACME project).
     * @param name
     *            The module name. Automatic module detection is not supported here.
     * @param newName
     *            The new name for the module.
     * @param output
     *            Output interface.
     */
    public static void renameModule(String directory, String name, String newName, IOutput output)
    {
        ACMEModule.renameModule(directory, name, newName, output);
    }

    /**
     * Deletes a module.
     * 
     * @param directory
     *            The current directory. This directory must be inside a valid ACME module (unless moduleName is != null, then it must only be inside a valid ACME project).
     * @param name
     *            The module name. Leave 'null' for automatic module detection.
     * @param output
     *            Output interface.
     */
    public static void deleteModule(String directory, String name, IOutput output)
    {
        ACMEModule.deleteModule(directory, name, output);
    }

    /**
     * Creates a new file.
     * 
     * @param directory
     *            The current directory. This directory must be inside a valid ACME module (unless moduleName is != null, then it must only be inside a valid ACME project).
     * @param name
     *            The name of the file.
     * @param type
     *            The type of the file.
     * @param moduleName
     *            Optional name of the module. Leave 'null' for automatic module detection.
     * @param isPublic
     *            Indicates if the header file should be public.
     * @param withTests
     *            Indicate if test files should get generated.
     * @param withHeader
     *            Indicates if a header file should get generated.
     * @param withSource
     *            Indicates if a source file should get generated.
     * @param output
     *            Output interface.
     */
    public static void createFile(String directory, String name, FileType type, String moduleName, boolean isPublic, boolean withTests, boolean withHeader, boolean withSource, IOutput output)
    {
        ACMEFile.createFile(directory, name, type, moduleName, isPublic, withTests, withHeader, withSource, output);
    }

    /**
     * Renames a file.
     * 
     * @param directory
     *            The current directory. This directory must be inside a valid ACME module (unless moduleName is != null, then it must only be inside a valid ACME project).
     * @param name
     *            The current name of the file.
     * @param moduleName
     *            Optional name of the module. Leave 'null' for automatic module detection.
     * @param newName
     *            The new name of the file.
     * @param output
     *            Output interface.
     */
    public static void renameFile(String directory, String name, String moduleName, String newName, IOutput output)
    {
        ACMEFile.renameFile(directory, name, moduleName, newName, output);
    }

    /**
     * Deletes a file.
     * 
     * @param directory
     *            The current directory. This directory must be inside a valid ACME module (unless moduleName is != null, then it must only be inside a valid ACME project).
     * @param name
     *            The current name of the file.
     * @param moduleName
     *            Optional name of the module. Leave 'null' for automatic module detection.
     * @param header
     *            True if header should get deleted.
     * @param source
     *            True if source should get deleted.
     * @param test
     *            True if test files should get deleted.
     * @param output
     *            Output interface.
     */
    public static void deleteFile(String directory, String name, String moduleName, boolean header, boolean source, boolean test, IOutput output)
    {
        ACMEFile.deleteFile(directory, name, moduleName, header, source, test, output);
    }

    /**
     * Lists all variables that can be used in templates.
     * 
     * @param output
     *            Output interface.
     */
    public static void listVariables(IOutput output)
    {
        output.writeLine("Listing available variables...");
        Map<String, String> vars = new HashMap<String, String>();
        TemplateHandling.fillAdditionalVariables(vars);
        for (Map.Entry<String, String> pair : vars.entrySet())
        {
            output.writeLine("    " + pair.getKey() + ": " + pair.getValue());
        }
    }

    /**
     * Downloads a new set of templates.
     * 
     * @param path
     *            The path from which templates are loaded.
     * @param check
     *            True if a check should be performed, but no download.
     * @param output
     *            Output interface.
     */
    public static void downloadTemplates(String path, boolean force, boolean check, IOutput output)
    {
        ACMEDownload.downloadTemplates(path, force, check, output);
    }

    /**
     * Prints all unused files of the specified module.
     * 
     * @param directory
     *            The current directory. This directory must be inside a valid ACME module (unless moduleName is != null, then it must only be inside a valid ACME project).
     * @param moduleName
     *            Optional name of the module. Leave 'null' for automatic module detection.
     * @param printCommand
     *            True to print the add-file command immediately.
     * @param output
     *            Output interface.
     */
    public static void printUnusedFiles(String directory, String moduleName, boolean printCommand, IOutput output)
    {
        ACMEUnusedFiles.printUnusedFiles(directory, moduleName, printCommand, output);
    }

    /**
     * Refreshes the CMake project.
     * 
     * @param directory
     *            The current directory. This directory must be inside a valid ACME module (unless moduleName is != null, then it must only be inside a valid ACME project).
     * @param generatorName
     *            The CMake generator.
     * @param toolchainFile
     *            The toolchain filename.
     * @param buildFolderName
     *            Name of the build folder.
     * @param deliverablefolderName
     *            Name of the deliverable folder.
     * @param output
     *            Output interface.
     */
    public static void refreshProject(String directory, String generatorName, String toolchainFile, String buildFolderName, String deliverablefolderName, IOutput output)
    {
        ACMEProject.refreshProject(directory, generatorName, toolchainFile, buildFolderName, deliverablefolderName, output);
    }
}
