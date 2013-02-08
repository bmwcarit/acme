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

import de.bmw.carit.acme.api.ACMEDownload;
import de.bmw.carit.acme.api.Constants;

public class ThirdPartySoftwareCommand extends Command
{
    @Override
    public void execute(String[] args)
    {
        // read out file creation arguments
        String path = ArgumentUtils.getArgumentValue(args, "-p", Constants.DEFAULT_3PSW_PATH);
        boolean list = ArgumentUtils.hasArgument(args, "-list");
        if (list)
        {
            ACMEDownload.list3psw(path, new ConsoleOutput());
            return;
        }
        else
        {
            String software = ArgumentUtils.getArgumentValue(args, "-DOWNLOAD");
            if (software == null)
            {
                printUsage();
                return;
            }
            ACMEDownload.download3psw(ArgumentUtils.getCurrentDirectory(), path, software, new ConsoleOutput());
        }

    }

    @Override
    public String getDescription()
    {
        return "Provides access to third party software";
    }

    @Override
    protected void printUsage()
    {
        System.out.println("ThirdPartySoftware usage:");
        System.out.println("    acme 3psw [-LIST | -DOWNLOAD=NAME] [-p=PATH]");
        System.out.println("        -LIST lists the available software");
        System.out.println("        -DOWNLOAD retrieves the specified software");
    }

    @Override
    public String getCommand()
    {
        return "3psw";
    }
}
