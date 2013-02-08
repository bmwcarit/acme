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

/**
 * Access to constant values.
 */
public class Constants
{
    public static final String SRC_FOLDER_NAME = "src";
    public static final String INCLUDE_FOLDER_NAME = "include";
    public static final String TEST_FOLDER_NAME = "test";
    public static final String CMAKELISTS_FILENAME = "CMakeLists.txt";
    public static final String TEMPLATES_FOLDERNAME = "templates";
    public static final String MODULES_FOLDERNAME = "modules";
    public static final String THIRDPARTYSOFTWARE_FOLDERNAME = "3psw";
    public static final String CMAKE_FOLDERNAME = "cmake";
    public static final String NEW_LINE = System.getProperty("line.separator");
    public static final String CHARSET = "US-ASCII";
    public static final String DEFAULT_DOWNLOAD_PATH = "\\\\dc01\\Documents\\Austausch\\jACME\\jACME-templates";
    public static final String DEFAULT_3PSW_PATH = "\\\\dc01\\Documents\\Austausch\\jACME\\3psw-downloads";
    public static final String ACME_TEMPLATE_ZIP_FILENAME = "ACME.template.zip";
    public static final String TEMPLATES_BACKUP_FOLDER_NAME = "backuptemplates";
}
