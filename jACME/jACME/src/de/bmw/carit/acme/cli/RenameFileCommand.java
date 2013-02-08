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

public class RenameFileCommand extends Command
{
    @Override
    public void execute(String[] args)
    {
        // read out file creation arguments
        String name = ArgumentUtils.argument(args, 0, null);
        if (name == null)
        {
            printUsage();
            return;
        }
        if (name.startsWith("-"))
        {
            printUsage();
            return;
        }

        // read out file creation arguments
        String newName = ArgumentUtils.argument(args, 1, null);
        if (newName == null)
        {
            printUsage();
            return;
        }
        if (newName.startsWith("-"))
        {
            printUsage();
            return;
        }

        String moduleName = ArgumentUtils.getArgumentValue(args, "-m");
        ACME.renameFile(ArgumentUtils.getCurrentDirectory(), name, moduleName, newName, new ConsoleOutput());
    }

    @Override
    public String getDescription()
    {
        return "Renames a file";
    }

    @Override
    public void printUsage()
    {
        System.out.println("RenameFile usage:");
        System.out.println("    acme rf NAME NEWNAME [-m=MODULENAME]");
        System.out.println("        -m=MODULENAME is optional is defines an alternative module in which the file is created");
    }

    @Override
    public String getCommand()
    {
        return "rf";
    }
}
