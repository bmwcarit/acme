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

package de.bmw.carit.acme.api;

/**
 * Represents a module type.
 */
public enum ModuleType
{
    /**
     * A static module (aka: lib).
     */
    Static,

    /**
     * A dynamic module (aka: dll).
     */
    Dynamic,

    /**
     * An executable (aka: exe).
     */
    Exe;

    /**
     * Parses a string and returns the representing module type.
     * 
     * @param type
     *            The type. If null or unknown, 'Static' is returned.
     * @return The representing module type as enumeration member.
     */
    public static ModuleType fromString(String type)
    {
        if (type != null)
        {
            if (type.equalsIgnoreCase(Dynamic.toString()))
            {
                return Dynamic;
            }
            if (type.equalsIgnoreCase(Exe.toString()))
            {
                return Exe;
            }
        }
        return Static; // default
    }
}
