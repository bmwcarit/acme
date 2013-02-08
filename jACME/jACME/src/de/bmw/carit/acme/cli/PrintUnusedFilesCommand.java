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

class PrintUnusedFilesCommand extends Command
{
    @Override
    public void execute(String[] args)
    {
        // read out module name
        String name = ArgumentUtils.getArgumentValue(args, "-m");
        boolean printCommand = ArgumentUtils.hasArgument(args, "-printCommand");

        // API call
        ACME.printUnusedFiles(ArgumentUtils.getCurrentDirectory(), name, printCommand, new ConsoleOutput());
    }

    @Override
    public String getDescription()
    {
        return "Lists all unsued files inside a module.";
    }

    @Override
    public void printUsage()
    {
        System.out.println("PrintUnusedFiles usage:");
        System.out.println("    acme pf [-m=MODULENAME] [-printCommand]");
        System.out.println("        -m=MODULENAME is optional and defines an alternative module in which the file is created");
        System.out.println("        -printCommand is optional and defines if the ADD_FILE commands are printed immediately");
        System.out.println("        If you call pf outside a module but inside a project, all modules will get scanned.");
    }

    @Override
    public String getCommand()
    {
        return "pf";
    }
}
