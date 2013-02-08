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
import de.bmw.carit.acme.api.ModuleType;

class CreateModuleCommand extends Command
{
    @Override
    public void execute(String[] args)
    {
        // read out module name and type
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
        String type = ArgumentUtils.getArgumentValue(args, "-t", ModuleType.Static.toString());

        // API call
        ACME.createModule(ArgumentUtils.getCurrentDirectory(), name, ModuleType.fromString(type), new ConsoleOutput());
    }

    @Override
    public String getDescription()
    {
        return "Creates a new module inside a project";
    }

    @Override
    public void printUsage()
    {
        System.out.println("CreateModule usage:");
        System.out.println("    acme cm NAME [-t=TYPE]");
        System.out.println("        -t=TYPE is optional is by default 'static' (also available: 'dynamic' and 'exe')");
        System.out.println("    In the templates, the variables 'projectName', 'moduleName' and 'moduleType' are provided.");
    }

    @Override
    public String getCommand()
    {
        return "cm";
    }
}
