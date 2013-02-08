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

class RefreshProjectCommand extends Command
{
    @Override
    public void execute(String[] args)
    {
        String toolchain = ArgumentUtils.getArgumentValue(args, "-t", "Windows_X86_32.toolchain");
        String generator = ArgumentUtils.getArgumentValue(args, "-g", "Visual Studio 10");
        String buildFolderName = ArgumentUtils.getArgumentValue(args, "-b", "build");
        String deliverablefolderName = ArgumentUtils.getArgumentValue(args, "-d", "deliverable");

        // API call
        ACME.refreshProject(ArgumentUtils.getCurrentDirectory(), generator, toolchain, buildFolderName, deliverablefolderName, new ConsoleOutput());
    }

    @Override
    public String getDescription()
    {
        return "Refreshes the current ACME project. Deletes the build and deliverable folder and recreated the build files.";
    }

    @Override
    public void printUsage()
    {
        System.out.println("RefreshProject usage:");
        System.out.println("    acme rp [-t=toolchainname] [-g=generatorname] [-b=buildfoldername] [-d=deliverablefoldername]");
        System.out.println("    Deletes the build and deliverable folder and recreates the build folder. The 'toolchainname' param defines the plain name of the file.");
        System.out.println("    Default values are 'Windows_X86_32.toolchain' and 'Visual Studio 10'.");
        System.out.println("    buildfoldername and deliverablefoldername describe the name of the build and deliverable folder.");
        System.out.println("    Default values are 'build' and 'deliverable'.");
    }

    @Override
    public String getCommand()
    {
        return "rp";
    }
}
