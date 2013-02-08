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

/**
 * Base class for jACME console commands. A command interprets command line arguments and redirects to the correct jACME-API call.
 * 
 * Used in Program.java to setup the possible commands.
 */
abstract class Command
{

    /**
     * Called when the command was used in the command line invocation.
     * 
     * @param args
     *            The arguments for the command.
     */
    public abstract void execute(String[] args);

    /**
     * Gets a short description of the command which is shows in the command overview list.
     * 
     * @return A short description of the command.
     */
    public abstract String getDescription();

    /**
     * Helper method for the command if the 'execute' method cannot succeed because of invalid arguments. Should print out the correct arguments usage.
     */
    protected abstract void printUsage();

    /**
     * Gets the command string for the command.
     * 
     * @return The command string.
     */
    public abstract String getCommand();
}
