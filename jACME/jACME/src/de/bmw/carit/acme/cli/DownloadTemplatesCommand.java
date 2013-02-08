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

package de.bmw.carit.acme.cli;

import de.bmw.carit.acme.api.ACME;
import de.bmw.carit.acme.api.Constants;

public class DownloadTemplatesCommand extends Command
{
    @Override
    public void execute(String[] args)
    {
        String path = ArgumentUtils.getArgumentValue(args, "-p", Constants.DEFAULT_DOWNLOAD_PATH);
        boolean force = ArgumentUtils.hasArgument(args, "-force");
        boolean check = ArgumentUtils.hasArgument(args, "-check");
        if (force && check)
        {
            // both is not allowed
            printUsage();
            return;
        }
        ACME.downloadTemplates(path, force, check, new ConsoleOutput());
    }

    @Override
    public String getDescription()
    {
        return "Downloads a new set of templates (if available) from a specified file path. A backup copy of the current templates is performed";
    }

    @Override
    protected void printUsage()
    {
        System.out.println("DownloadTemplates usage:");
        System.out.println("    acme dt [-p=PATH] [-CHECK | -FORCE]");
        System.out.println("        -p=PATH is optional and by default '" + Constants.DEFAULT_DOWNLOAD_PATH + "'");
        System.out.println("        -CHECK is optional and will only check if a newer version is available, but will not download it");
        System.out.println("        -FORCE is optional and will force a download even if the current version seems up to date");
    }

    @Override
    public String getCommand()
    {
        return "dt";
    }
}
