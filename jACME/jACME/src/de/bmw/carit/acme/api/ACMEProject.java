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
import java.util.HashMap;
import java.util.Map;

/**
 * Wraps the actions to create a project.
 */
class ACMEProject
{
    public static boolean refreshProject(String directory, String generatorName, String toolchainFile, String buildFolderName, String deliverablefolderName, IOutput output)
    {
        try
        {
            File projectDir = FileUtils.getProjectFile(directory);
            if (projectDir == null)
            {
                output.writeLine("    Current folder seems not to be inside a valid ACME project: " + directory);
                return false;
            }
            output.writeLine("Refreshing project " + projectDir.getName());

            File buildFolder = new File(projectDir, buildFolderName);
            File deliverableFolder = new File(projectDir, deliverablefolderName);

            output.writeLine("    Deleting build and deliverable folder...");
            FileUtils.deleteDirectory(deliverableFolder);
            FileUtils.deleteDirectory(buildFolder);
            buildFolder.mkdirs();

            File toolchain = FileUtils.findFile(projectDir, toolchainFile);

            output.writeLine("    Running CMake...");
            output.writeLine("        Toolchain file is " + toolchain.getAbsolutePath());
            output.writeLine("        Generator is " + generatorName);
            ProcessBuilder builder = new ProcessBuilder();
            builder.command("cmake", "..", "-DCMAKE_TOOLCHAIN_FILE=" + toolchain.getAbsolutePath().replace("\\", "/"), "-G", "\"" + generatorName + "\"", "-DPLUGIN_FRAMEWORK_ROOT_DIR=\"" + projectDir.getAbsolutePath().replace("\\", "/") + "\"");
            builder.directory(buildFolder);
            builder.inheritIO();
            Process cmakeGenerate = builder.start();
            int retVal = cmakeGenerate.waitFor();
            if (retVal != 0) // cmake error
            {
                output.writeLine("    CMake exited with error code " + retVal + ". Please do a manual check.");
                return false;
            }
        }
        catch (Exception ex)
        {
            throw new IllegalArgumentException("Could not recreate project", ex);
        }
        return true;
    }

    public static boolean createProject(String directory, String name, IOutput output)
    {
        // initial checks
        output.writeLine("Creating project '" + name + "'...");
        if (!FileUtils.isNameOk(name))
        {
            // name has invalid characters
            output.writeLine(String.format("    The project name '%s' is invalid.", name));
            return false;
        }

        // only create it once
        File projectDirName = new File(directory, name);
        if (projectDirName.exists())
        {

            output.writeLine("    Project seems to exist. Delete it first.");
            return false;
        }
        if (isAlreadyProject(projectDirName))
        {
            output.writeLine("    Cannot create nested projects.");
            return false;
        }

        // do the work
        setupProjectStructure(projectDirName, output);
        createCMakeLists(name, projectDirName, output);

        // done
        return true;
    }

    private static boolean isAlreadyProject(File projectDirName)
    {
        // perform a simple check
        return new File(projectDirName, Constants.CMAKELISTS_FILENAME).exists();
    }

    private static void createCMakeLists(String name, File projectDirName, IOutput output)
    {
        output.writeLine("    Creating CMakeLists.txt");
        Map<String, String> variables = new HashMap<String, String>();
        variables.put("projectName", name);
        String projectContent = TemplateHandling.getContent("Project.template", variables, projectDirName);
        FileUtils.writeAllText(new File(projectDirName, Constants.CMAKELISTS_FILENAME), projectContent);
    }

    private static void setupProjectStructure(File projectDirName, IOutput output)
    {
        try
        {
            // create some directories
            output.writeLine("    Setup project structure");
            projectDirName.mkdirs();
            new File(projectDirName, Constants.THIRDPARTYSOFTWARE_FOLDERNAME).mkdirs();
            File modulesDir = new File(projectDirName, Constants.MODULES_FOLDERNAME);
            modulesDir.mkdirs();
            new File(modulesDir, Constants.CMAKELISTS_FILENAME).createNewFile();
            File cmakeDirName = new File(projectDirName, Constants.CMAKE_FOLDERNAME);
            cmakeDirName.mkdirs();

            // unzip initial ACME
            output.writeLine("    Unzipping ACME");
            TemplateHandling.unzipTemplateZip(Constants.ACME_TEMPLATE_ZIP_FILENAME, cmakeDirName);
        }
        catch (IOException e)
        {
            throw new IllegalStateException(e);
        }
    }
}
