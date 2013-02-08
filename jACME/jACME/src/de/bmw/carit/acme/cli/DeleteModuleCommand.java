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

public class DeleteModuleCommand extends Command
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

        ACME.deleteModule(ArgumentUtils.getCurrentDirectory(), name, new ConsoleOutput());
    }

    @Override
    public String getDescription()
    {
        return "Deletes a module";
    }

    @Override
    protected void printUsage()
    {
        System.out.println("DeleteModule usage:");
        System.out.println("    acme dm MODULENAME");
    }

    @Override
    public String getCommand()
    {
        return "dm";
    }

}
