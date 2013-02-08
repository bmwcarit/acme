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
import java.io.FileInputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Contains the handling of template-related stuff like loading template-file contents or unzipping template zips.
 */
class TemplateHandling
{

    /**
     * Gets a file content from a file located in the templates folder.
     * 
     * @param filename
     *            The filename.
     * @param variables
     *            The variables to replace in the file contents. May be null. Additional variables will get automatically filled in.
     * @return The file contents with replaced variables.
     */
    public static String getContent(String filename, Map<String, String> variables, File currentDir)
    {
        if (variables == null)
        {
            variables = new HashMap<String, String>();
        }
        fillAdditionalVariables(variables);
        String templateContent = FileUtils.readAllText(getTemplateFile(filename, currentDir));
        templateContent = replaceVars(variables, templateContent);
        return templateContent;
    }

    private static File getTemplateFile(String filename, File currentDir)
    {
        // project specific path
        File projectSpecificAcmeFolder = new File(FileUtils.getProjectFile(currentDir.getAbsolutePath()), Constants.CMAKE_FOLDERNAME);
        File projectSpecificTemplateFolder = new File(projectSpecificAcmeFolder, Constants.TEMPLATES_FOLDERNAME);
        File projectSpecificTemplateFile = new File(projectSpecificTemplateFolder, filename);
        if (projectSpecificTemplateFile.exists())
        {
            return projectSpecificTemplateFile;
        }

        // default path
        return new File(getTemplatesFolder(), filename);
    }

    private static String replaceVars(Map<String, String> variables, String templateContent)
    {
        for (Map.Entry<String, String> pair : variables.entrySet())
        {
            templateContent = templateContent.replace("${" + pair.getKey() + ".toUpperCase}", pair.getValue().toUpperCase());
            templateContent = templateContent.replace("${" + pair.getKey() + ".toLowerCase}", pair.getValue().toLowerCase());
            templateContent = templateContent.replace("${" + pair.getKey() + "}", pair.getValue());
        }
        return templateContent;
    }

    /**
     * Gets a property value from a property file located in the templates folder.
     * 
     * @param filename
     *            The name of the property file.
     * @param propertyId
     *            The property id.
     * @param variables
     *            The variables to replace in the property value. May be null. Additional variables will get automatically filled in.
     * @return The property value with replaces variables.
     */
    public static String getProperty(String filename, String propertyId, Map<String, String> variables, File currentDir)
    {
        if (variables == null)
        {
            variables = new HashMap<String, String>();
        }
        fillAdditionalVariables(variables);
        try
        {
            Properties p = new Properties();
            FileInputStream stream = new FileInputStream(getTemplateFile(filename, currentDir));
            p.load(stream);
            stream.close();
            String id = p.getProperty(propertyId);
            if (id == null)
            {
                throw new IllegalArgumentException("property '" + propertyId + "'was not found in file " + filename);
            }
            id = replaceVars(variables, id);
            return id;
        }
        catch (Exception e)
        {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Gets the folder in which the templates are located.
     * 
     * @return The folder in which the templates are located.
     */
    public static File getTemplatesFolder()
    {
        String assemblyLocation = TemplateHandling.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        File assemblyPath = new File(assemblyLocation).getParentFile();
        return new File(assemblyPath, Constants.TEMPLATES_FOLDERNAME);
    }

    /**
     * Fills in the variables that are always usable in templates.
     * 
     * @param vals
     *            The map that get's filled.
     */
    public static void fillAdditionalVariables(Map<String, String> vals)
    {
        if (vals == null)
        {
            // nothing to do
            return;
        }
        for (Map.Entry<String, String> de : System.getenv().entrySet())
        {
            // add every environment variable
            vals.put(de.getKey(), de.getValue());
        }

        // additional year variable
        Calendar cal = GregorianCalendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        vals.put("currentYear", "" + year);
    }

    /**
     * Unzips a file in the templates folder.
     * 
     * @param filename
     *            The filename.
     * @param destinationDirectory
     *            The destination directory.
     */
    public static void unzipTemplateZip(String filename, File destinationDirectory)
    {
        File templatesFolder = getTemplatesFolder();
        File file = new File(templatesFolder, filename);
        if (!file.exists())
        {
            templatesFolder = new File(templatesFolder.getParentFile(), "release/templates");
            file = new File(templatesFolder, filename);
        }
        FileUtils.unzipContent(file, destinationDirectory);
    }
}
