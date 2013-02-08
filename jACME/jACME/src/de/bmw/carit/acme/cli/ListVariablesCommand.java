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

class ListVariablesCommand extends Command
{
    @Override
    public void execute(String[] args)
    {
        ACME.listVariables(new ConsoleOutput());
    }

    @Override
    public String getDescription()
    {
        return "Lists the variables currently available";
    }

    @Override
    public void printUsage()
    {
        System.out.println("ListVariables usage:");
        System.out.println("    acme vars");
    }

    @Override
    public String getCommand()
    {
        return "vars";
    }
}
