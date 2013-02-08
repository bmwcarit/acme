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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Contains helper methods for jACME.
 */
class FileUtils
{
    /**
     * Writes all given content into the file. Replaces already existing content.
     * 
     * @param file
     *            The file.
     * @param content
     *            The new content.
     */
    public static void writeAllText(File file, String content)
    {
        try
        {
            FileOutputStream fos = new FileOutputStream(file, false);
            fos.write(new String(content.getBytes("US-ASCII")).getBytes());
            fos.flush();
            fos.close();
        }
        catch (Exception e)
        {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Writes all given lines into the file. Replaces already existing content.
     * 
     * @param file
     *            The file.
     * @param newlines
     *            The new lines.
     */
    public static void writeAllLines(File file, String[] newlines)
    {
        StringBuilder b = new StringBuilder();
        for (String s : newlines)
        {
            b.append(s);
            b.append("\n");
        }
        writeAllText(file, b.toString());
    }

    /**
     * Appends the given content to the specified file.
     * 
     * @param file
     *            The file.
     * @param content
     *            The string content.
     */
    public static void appendAllText(File file, String content)
    {
        try
        {
            FileOutputStream fos = new FileOutputStream(file, true); // true means append
            fos.write(new String(content.getBytes("US-ASCII")).getBytes());
            fos.flush();
            fos.close();
        }
        catch (Exception e)
        {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Deletes a directory.
     * 
     * @param directory
     *            The directory.
     */
    public static void deleteDirectory(String directory)
    {
        File f = new File(directory);
        deleteDirectory(f);
    }

    /**
     * Deletes a directory.
     * 
     * @param directory
     *            The directory.
     */
    public static void deleteDirectory(File directory)
    {
        if (directory == null)
        {
            return;
        }
        File[] files = directory.listFiles();
        if (files == null)
        {
            files = new File[0];
        }
        for (File child : files)
        {
            if (child.isDirectory())
            {
                deleteDirectory(child);
            }
            else
            {
                // ignore errors, do best effort deletion
                child.delete();

                /*
                 * if (!child.delete()) { throw new IllegalStateException("Could not delete file " + child.getAbsolutePath()); }
                 */
            }
        }
        if (directory.exists())
        {
            directory.delete();
            /*
             * if (!directory.delete()) { throw new IllegalStateException("Could not delete directory " + directory.getAbsolutePath()); }
             */
        }
    }

    /**
     * Copies the contents of one directory into the other. Will not change modified flags of files.
     * 
     * @param sourceDir
     *            The source directory.
     * @param destinationDir
     *            The destination directory.
     */
    public static void copyDirectory(File sourceDir, File destinationDir)
    {
        if (sourceDir == null || !sourceDir.exists() || destinationDir == null)
        {
            return;
        }
        destinationDir.mkdirs();
        File[] files = sourceDir.listFiles();
        if (files == null)
        {
            files = new File[0];
        }
        for (File sourceFile : files)
        {
            if (sourceFile.isDirectory())
            {
                // recursive copy of directories
                File newDestSubDir = new File(destinationDir, sourceFile.getName());
                newDestSubDir.mkdirs();
                newDestSubDir.setLastModified(sourceFile.lastModified());
                copyDirectory(sourceFile, newDestSubDir);
            }
            else
            {
                // copy file
                File destFile = new File(destinationDir, sourceFile.getName());
                copyFile(sourceFile, destFile);
                destFile.setLastModified(sourceFile.lastModified()); // don't change modified flag!
            }
        }
    }

    private static void copyFile(File sourceFile, File destFile)
    {
        try
        {
            FileInputStream source = new FileInputStream(sourceFile);
            FileOutputStream destination = new FileOutputStream(destFile);

            FileChannel sourceFileChannel = source.getChannel();
            FileChannel destinationFileChannel = destination.getChannel();
            sourceFileChannel.transferTo(0, sourceFileChannel.size(), destinationFileChannel);

            source.close();
            destination.close();
        }
        catch (Exception ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    private static String trim(String str)
    {
        // trims some invalid characters at the beginning of the string.
        // happens if the templates are in an invalid file format.
        byte[] bytes = str.getBytes();
        for (int i = 0; i < bytes.length; i++)
        {
            if (bytes[i] > 0)
            {
                // first ok index found
                return new String(bytes, i, bytes.length - i);
            }
        }
        return str;
    }

    /**
     * Checks if the current directory is the main project directory.
     * 
     * @param directory
     *            The directory to check.
     * @return True if the directory is the main project directory.
     */
    private static boolean isProjectDir(File directory)
    {
        File dir = new File(directory, Constants.MODULES_FOLDERNAME);
        File cmakelists = new File(dir, Constants.CMAKELISTS_FILENAME);
        if (dir.exists() && cmakelists.exists())
        {
            // will be true if we are in project dir
            return true;
        }
        return false;
    }

    private static boolean lineAlreadyExists(String[] lines, String addFileCommand)
    {
        for (String str : lines)
        {
            if (str.trim().equalsIgnoreCase(addFileCommand))
            {
                return true;
            }
        }
        return false;

    }

    /**
     * Reads all text of a file.
     * 
     * @param file
     *            The file.
     * @return The content as string.
     */
    public static String readAllText(File file)
    {
        if (file == null || !file.exists())
        {
            throw new IllegalArgumentException("Invalid file given: " + file);
        }
        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader(new FileReader(file));
            String line = null;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = reader.readLine()) != null)
            {
                stringBuilder.append(line);
                stringBuilder.append("\n");
            }
            return trim(stringBuilder.toString());
        }
        catch (Exception e)
        {
            throw new IllegalStateException(e);
        }
        finally
        {
            if (reader != null)
            {
                try
                {
                    reader.close();
                }
                catch (IOException e)
                {
                    throw new IllegalStateException(e);
                }
            }
        }
    }

    /**
     * Reads all text of a file and returns the lines as string array.
     * 
     * @param file
     *            The file.
     * @return The content of the file as string array.
     */
    public static String[] readAllLines(File file)
    {
        if (file == null || !file.exists())
        {
            throw new IllegalArgumentException("Invalid file given: " + file);
        }
        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader(new FileReader(file));
            String line = null;
            List<String> stringBuilder = new ArrayList<String>();
            while ((line = reader.readLine()) != null)
            {
                stringBuilder.add(trim(line));
            }
            return stringBuilder.toArray(new String[stringBuilder.size()]);
        }
        catch (Exception e)
        {
            throw new IllegalStateException(e);
        }
        finally
        {
            if (reader != null)
            {
                try
                {
                    reader.close();
                }
                catch (IOException e)
                {
                    throw new IllegalStateException(e);
                }
            }
        }
    }

    /**
     * Gets the name of the project which in in the current directory (or in one above).
     * 
     * @param directory
     *            The directory to start searching for the project name.
     * @return The name or null if no project was found.
     */
    public static String getProjectName(String directory)
    {
        File projectFile = getProjectFile(directory);
        if (projectFile != null)
        {
            String content = readAllText(new File(projectFile, Constants.CMAKELISTS_FILENAME));
            int index = content.toLowerCase().indexOf("project(");
            if (index > 0)
            {
                index += "project(".length();
                String projName = content.substring(index);
                index = projName.indexOf(")");
                if (index > 0)
                {
                    projName = projName.substring(0, index);
                    // now select first component
                    String[] names = projName.split(" ");
                    if (names.length > 0)
                    {
                        return names[0];
                    }
                    return projName;
                }
            }

            return projectFile.getName();
        }
        return null;
    }

    /**
     * Gets the directory in which the main project is located.
     * 
     * @param directory
     *            The current directory.
     * @return The directory file or null if it was not found.
     */
    public static File getProjectFile(String directory)
    {
        File modulesDir = findModulesDir(directory);
        if (modulesDir != null)
        {
            try
            {
                return new File(modulesDir.getParentFile().getCanonicalPath()).getAbsoluteFile();
            }
            catch (IOException e)
            {
                return null;
            }
        }
        return null;
    }

    /**
     * Finds the 'modules' directory.
     * 
     * @param directory
     *            The directory from which the search is started.
     * @return The modules directory or null if it was not found.
     */
    public static File findModulesDir(String directory)
    {
        File dir = new File(directory);
        while (!isProjectDir(dir))
        {
            dir = dir.getParentFile();
            if (dir == null)
            {
                return null;
            }
        }
        return new File(dir, Constants.MODULES_FOLDERNAME);
    }

    /**
     * Concatenates the strings into one directory. Strings may be empty to indicate a blank directory.
     * 
     * @param startDir
     *            The start-file.
     * @param dirs
     *            A list of directories.
     * @return The file object pointing to the specified directory.
     */
    public static File concatenatePath(File startDir, String... dirs)
    {
        for (String dir : dirs)
        {
            startDir = new File(startDir, dir);
        }
        return startDir;
    }

    /**
     * Adds an 'ADD_FILE' entry into a module-CMakeLists.txt
     * 
     * @param moduleDirName
     *            The directory of the module.
     * @param fileName
     *            The file name.
     * @param output
     *            Output interface.
     */
    public static void adjustModuleCMakeLists(File moduleDirName, String fileName, boolean fileWasCreated, IOutput output)
    {
        output.writeLine("    Registering file in module specific CMakeLists");
        File cmakelistsFile = new File(moduleDirName, Constants.CMAKELISTS_FILENAME);
        if (!cmakelistsFile.exists())
        {
            try
            {
                cmakelistsFile.createNewFile();
            }
            catch (IOException e)
            {
                throw new IllegalStateException(e);
            }
        }
        String[] lines = readAllLines(cmakelistsFile);
        Map<String, String> variables = new HashMap<String, String>();
        variables.put("fileName", fileName);
        String addFileCommand = TemplateHandling.getProperty("FileContents.properties", "ADD_FILE_TEMPLATE", variables, moduleDirName);
        if (lineAlreadyExists(lines, addFileCommand))
        {
            // no need to write, but we need to "change" the file so that a rebuild is performed
            if (fileWasCreated)
            {
                cmakelistsFile.setLastModified(System.currentTimeMillis());
            }
            output.writeLine("    No need to adjust CMakeLists, because file entry already exists.");
            return;
        }
        String[] newlines = new String[lines.length + 1];
        for (int i = 0; i < newlines.length; i++)
        {
            newlines[i] = "";
        }
        int index = 0;
        String addModulePrefix = TemplateHandling.getProperty("FileContents.properties", "ADD_MODULE_PREFIX_TEMPLATE", null, moduleDirName);
        boolean found = false;
        for (int i = 0; i < lines.length; i++)
        {
            newlines[index] = lines[i];
            index++;

            // insert right after module definition
            if (lines[i].trim().toLowerCase().contains(addModulePrefix.toLowerCase()))
            {
                // add our entry right after it!
                newlines[index] = addFileCommand;
                index++;
                found = true;
            }
        }

        if (found)
        {
            writeAllLines(cmakelistsFile, newlines);
        }
        else
        {
            output.writeLine("    Could not insert text in the CMakeLists file. It remains unchanged.");
        }
    }

    /**
     * Performs a name check if the name is a valid folder- or filename.
     * 
     * @param name
     *            The name.
     * @return True if the name is ok.
     */
    public static boolean isNameOk(String name)
    {
        if (name == null || name.contains("#") || name.contains("/") || name.contains(" ") || name.contains("\\"))
        {
            return false;
        }
        return true;
    }

    /**
     * Removes a file extension from a filename.
     * 
     * @param name
     *            The filename.
     * @return The filename with removed extension.
     */
    public static String removeExtension(String name)
    {
        int index = name.lastIndexOf(".");
        if (index != -1)
        {
            name = name.substring(0, index);
        }
        return name;
    }

    /**
     * Gets the extension of a file.
     * 
     * @param name
     *            The file name.
     * @return The extension.
     */
    public static String getExtension(String name)
    {
        int index = name.lastIndexOf(".");
        if (index != -1)
        {
            name = name.substring(index, name.length());
        }
        return name;
    }

    /**
     * Finds the path for a given module.
     * 
     * @param directory
     *            The starting directory.
     * @param moduleName
     *            The name of the module.
     * @return The module path or null if it was not found.
     */
    public static File findModuleDir(String directory, String moduleName)
    {
        File modulesDir = findModulesDir(directory);
        if (modulesDir != null)
        {
            return new File(modulesDir, moduleName);
        }
        return null;
    }

    /**
     * Find the path of the module of the given directory.
     * 
     * @param directory
     *            The search directory
     * @return The module dir or null, if the directory was not found.
     */
    public static File findCurrentModuleDir(String directory)
    {
        // will find something by traversing up
        File current = new File(directory);
        if (current.getName().equalsIgnoreCase("."))
        {
            // strange bugfix here...
            current = current.getParentFile();
        }
        if (current == null || current.getParentFile() == null)
        {
            return null;
        }
        while (current.getParentFile() != null)
        {
            if (current.exists() && current.getParentFile().getName().equals(Constants.MODULES_FOLDERNAME) && new File(current, Constants.CMAKELISTS_FILENAME).exists())
            {
                return current;
            }
            current = current.getParentFile();
        }
        return null;
    }

    /**
     * Removes 'Test' from a file name.
     * 
     * @param name
     *            The filename.
     * @return The name without 'Test'.
     */
    public static String removeTest(String name)
    {
        if (!name.equalsIgnoreCase("test") && name.toLowerCase().endsWith("test"))
        {
            name = name.substring(0, name.length() - 4);
        }
        return name;
    }

    /**
     * Unzips a file.
     * 
     * @param file
     *            The file.
     * @param destinationDirectory
     *            The destination directory.
     */
    @SuppressWarnings("rawtypes")
    public static void unzipContent(File file, File destinationDirectory)
    {
        Enumeration entriesEnum;
        ZipFile zipFile;
        try
        {
            zipFile = new ZipFile(file);
            entriesEnum = zipFile.entries();
            if (!destinationDirectory.exists())
            {
                destinationDirectory.mkdir();
            }
            while (entriesEnum.hasMoreElements())
            {
                try
                {
                    ZipEntry entry = (ZipEntry) entriesEnum.nextElement();

                    if (entry.isDirectory())
                    {
                        new File(destinationDirectory, entry.getName()).mkdirs();
                    }
                    else
                    {
                        new File(destinationDirectory, entry.getName()).getParentFile().mkdirs();
                        FileUtils.writeFile(zipFile.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(new File(destinationDirectory, entry.getName()))));
                    }
                }
                catch (Exception e)
                {
                    zipFile.close();
                    throw new IllegalStateException(e);
                }
            }
            zipFile.close();
        }
        catch (IOException e)
        {
            throw new IllegalStateException(e);
        }
    }

    public static final void writeFile(InputStream in, OutputStream out) throws IOException
    {
        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) >= 0)
        {
            out.write(buffer, 0, len);
        }
        in.close();
        out.close();
    }

    private static File recFindFile(File startDir, String filename)
    {
        if (startDir.isDirectory())
        {
            File[] files = startDir.listFiles();
            if (files != null)
            {
                for (File file : files)
                {
                    File foundFile = recFindFile(file, filename);
                    if (foundFile != null)
                    {
                        return foundFile;
                    }
                }
            }
        }
        else
        {
            if (startDir.getName().equals(filename))
            {
                return startDir;
            }
        }
        return null;
    }

    public static File findFile(File startDir, String filename)
    {
        File file = recFindFile(startDir, filename);
        if (file == null)
        {
            throw new IllegalStateException("Could not find file " + filename + " in dir " + startDir);
        }
        return file;
    }

}
