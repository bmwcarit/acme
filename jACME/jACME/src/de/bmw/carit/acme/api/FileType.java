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
 * Represents a type of a file.
 */
public enum FileType
{
    /**
     * A cpp class file.
     */
    CPP
    {
        @Override
        public String getSourceFileExtension()
        {
            return ".cpp";
        }

        @Override
        public String getSourceTemplateName()
        {
            return "CPP.source.template";
        }

        @Override
        public String getHeaderTemplateName()
        {
            return "CPP.header.template";
        }
    },

    /**
     * A plain C file.
     */
    C
    {
        @Override
        public String getSourceFileExtension()
        {
            return ".c";
        }

        @Override
        public String getSourceTemplateName()
        {
            return "C.source.template";
        }

        @Override
        public String getHeaderTemplateName()
        {
            return "C.header.template";
        }
    };

    public abstract String getSourceFileExtension();

    public abstract String getSourceTemplateName();

    public abstract String getHeaderTemplateName();

    /**
     * Parses a string and returns the representing file type.
     * 
     * @param type
     *            The type. If null or unknown, 'Class' is returned.
     * @return The representing file type as enumeration member.
     */
    public static FileType fromString(String type)
    {
        if (type != null)
        {
            if (type.equalsIgnoreCase(C.toString()))
            {
                return C;
            }
        }
        return CPP; // default
    }
}
