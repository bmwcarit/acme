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
import de.bmw.carit.acme.api.FileType;

class CreateFileCommand extends Command
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
        boolean withTests = !ArgumentUtils.hasArgument(args, "-notest");
        boolean isPublic = !ArgumentUtils.hasArgument(args, "-private");
        boolean withHeader = !ArgumentUtils.hasArgument(args, "-noheader");
        boolean withSource = !ArgumentUtils.hasArgument(args, "-nosource");
        String moduleName = ArgumentUtils.getArgumentValue(args, "-m");
        String type = ArgumentUtils.getArgumentValue(args, "-t", FileType.CPP.toString());

        // API call
        // right now, we only support class files...
        ACME.createFile(ArgumentUtils.getCurrentDirectory(), name, FileType.fromString(type), moduleName, isPublic, withTests, withHeader, withSource, new ConsoleOutput());
    }

    @Override
    public String getDescription()
    {
        return "Creates a new file inside a module";
    }

    @Override
    public void printUsage()
    {
        System.out.println("CreateFile usage:");
        System.out.println("    acme cf NAME [-t=TYPE] [-m=MODULENAME] [-NOSOURCE] [-NOHEADER] [-NOTEST] [-PRIVATE]");
        System.out.println("        -NOTEST is optional is by default 'false'");
        System.out.println("        -t=TYPE defines the type of the file ('CPP' and 'C' are supported, default is cpp)");
        System.out.println("        -m=MODULENAME is optional is defines an alternative module in which the file is created");
        System.out.println("        -NOSOURCE diables the creation of a .cpp file, -NOHEADER disables the creation of a .h file, by default, both will get created");
        System.out.println("        -PRIVATE places the header file in the private header folder of the module, by default, the file is in the public header directory. Makes only sense if a header is created");
        System.out.println("    In the templates, the variables 'fileName', 'projectName' and 'moduleName' are provided.");
    }

    @Override
    public String getCommand()
    {
        return "cf";
    }
}
