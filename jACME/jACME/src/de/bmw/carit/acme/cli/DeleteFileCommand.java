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

public class DeleteFileCommand extends Command
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

        boolean deleteTest = ArgumentUtils.hasArgument(args, "-test");
        boolean deleteSource = ArgumentUtils.hasArgument(args, "-source");
        boolean deleteHeader = ArgumentUtils.hasArgument(args, "-header");
        String moduleName = ArgumentUtils.getArgumentValue(args, "-m");
        if (!deleteHeader && !deleteSource && !deleteTest)
        {
            // default case
            deleteHeader = true;
            deleteSource = true;
            deleteTest = true;
        }
        ACME.deleteFile(ArgumentUtils.getCurrentDirectory(), name, moduleName, deleteHeader, deleteSource, deleteTest, new ConsoleOutput());
    }

    @Override
    public String getDescription()
    {
        return "Deletes a file";
    }

    @Override
    protected void printUsage()
    {
        System.out.println("DeleteFile usage:");
        System.out.println("    acme df FILENAME [-TEST | -HEADER | -SOURCE]");
        System.out.println("        -TEST -HEADER -SOURCE define which file should get deleted. If none is specified, the function tries to delete every file.");
    }

    @Override
    public String getCommand()
    {
        return "df";
    }

}
