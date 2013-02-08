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

import java.io.File;

public class ArgumentUtils
{

    /**
     * Returns a subarray of the given array.
     * 
     * @param data
     *            The original array.
     * @param index
     *            The start index.
     * @param length
     *            The length that should get copied.
     * @return The subarray.
     */
    public static String[] subArray(String[] data, int index, int length)
    {
        if (index + length > data.length)
        {
            throw new IllegalArgumentException("invalid length of input array");
        }
        String[] result = new String[length];
        System.arraycopy(data, index, result, 0, length);
        return result;
    }

    /**
     * Gets an argument at the specified index.
     * 
     * @param args
     *            The arguments.
     * @param index
     *            The argument index.
     * @param defaultString
     *            The default value that is returned if the index does not match an argument.
     * @return The argument value or the default string if the index does not match.
     */
    public static String argument(String[] args, int index, String defaultString)
    {
        if (args.length > index)
        {
            return args[index];
        }
        return defaultString;
    }

    /**
     * Checks if an argument is present in the argument list.
     * 
     * @param args
     *            The arguments.
     * @param argument
     *            The argument to test.
     * @return True if the argument was found.
     */
    public static boolean hasArgument(String[] args, String argument)
    {
        for (String arg : args)
        {
            if (arg.toLowerCase().equals(argument.toLowerCase()))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets an argument value.
     * 
     * @param args
     *            The arguments.
     * @param argumentPrefix
     *            The argument prefix.
     * @param defaultValue
     *            The value that should get returned if the argument was not found.
     * @return The argument value or the default value if argument was not found.
     */
    public static String getArgumentValue(String[] args, String argumentPrefix, String defaultValue)
    {
        for (String arg : args)
        {
            if (arg.toLowerCase().startsWith(argumentPrefix.toLowerCase()))
            {
                int index = arg.indexOf("=");
                if (index != -1)
                {
                    return arg.substring(index + 1);
                }
            }
        }
        return defaultValue;
    }

    /**
     * Gets an argument value.
     * 
     * @param args
     *            The arguments.
     * @param argumentPrefix
     *            The argument prefix.
     * @return The argument value or null if argument was not found.
     * @see #getArgumentValue(String[], String, String)
     */
    public static String getArgumentValue(String[] args, String argumentPrefix)
    {
        return getArgumentValue(args, argumentPrefix, null);
    }

    /**
     * Gets the current directory.
     * 
     * @return The current directory.
     */
    public static String getCurrentDirectory()
    {
        return new File(".").getAbsolutePath();
    }

    /**
     * Trims '--' from all arguments.
     * 
     * @param args
     *            All arguments.
     */
    public static void trimDoubleMinus(String[] args)
    {
        for (int i = 0; i < args.length; i++)
        {
            args[i] = args[i].replace("--", "-");
        }
    }

}
