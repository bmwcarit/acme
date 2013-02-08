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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Main entry point for command line usage of jACME.
 * 
 * This class redirects calls to the corresponding commands. These commands interpret the arguments and redirect the call to the actual API call.
 */
public class Main
{
    static Map<String, Command> commands = new HashMap<String, Command>();

    private static void register(Command command)
    {
        commands.put(command.getCommand(), command);
    }

    static
    {
        register(new CreateProjectCommand());
        register(new RefreshProjectCommand());
        register(new CreateModuleCommand());
        register(new RenameModuleCommand());
        register(new DeleteModuleCommand());
        register(new CreateFileCommand());
        register(new RenameFileCommand());
        register(new DeleteFileCommand());
        register(new ListVariablesCommand());
        register(new VersionCommand());
        register(new DownloadTemplatesCommand());
        register(new ThirdPartySoftwareCommand());
        register(new PrintUnusedFilesCommand());
    }

    /**
     * Main entry point.
     * 
     * @param args
     *            Command line arguments.
     */
    public static void main(String[] args)
    {
        if (args.length == 0)
        {
            // no argument given, help is requested!
            printUsage();
            return;
        }

        Command command = commands.get(args[0].toLowerCase());
        args = ArgumentUtils.subArray(args, 1, args.length - 1);
        ArgumentUtils.trimDoubleMinus(args);
        if (command == null)
        {
            // unknown argument given
            printUsage();
            return;
        }
        if (ArgumentUtils.hasArgument(args, "-?") || ArgumentUtils.hasArgument(args, "-help"))
        {
            // help for command requested
            command.printUsage();
            return;
        }

        try
        {
            // execute the defined command
            command.execute(args);
        }
        catch (Exception ex)
        {
            // better error handling?
            System.err.println();
            System.err.println("Unexpected error. Please report this:");
            System.err.println();
            System.err.println("Command line: " + Arrays.toString(args));
            System.err.println("Exception:");
            ex.printStackTrace();
        }
    }

    private static void printUsage()
    {
        new VersionCommand().execute(null);
        System.out.println();
        System.out.println("Usage: 'acme [COMMAND] [ARGS]'");
        System.out.println();
        System.out.println("Possible commands are:");
        for (Map.Entry<String, Command> pair : commands.entrySet())
        {
            System.out.println("    " + pair.getKey() + ": " + pair.getValue().getDescription());
        }

        System.out.println();
        System.out.println("Type 'acme [COMMAND] -help' to get a detailed command usage description.");
    }
}
