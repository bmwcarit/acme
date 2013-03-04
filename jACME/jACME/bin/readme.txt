=====================================================================================================
License
=====================================================================================================

Copyright 2012 BMW Car IT GmbH

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

=====================================================================================================
jACME
=====================================================================================================

Command line interface to increase productivity while working with ACME projects.

Supports the creation of 
* Projects
* Modules (+ Renaming and deletion)
* Files (+ Renaming and deletion)
in an ACME project.

=====================================================================================================
Usage
=====================================================================================================

Make sure that the acme.bat or acme.sh is in your path.

To get a list of all available commands, type in your command line
> acme

For each listed command, you can get more detailed info if you append -help to the command, e.g.
> acme cp -help

=====================================================================================================
Available commands
=====================================================================================================

Template-Update
===============
> acme dt
Loads new file templates from a defined network location if newer ones are available.

> acme dt -force
Forces the download.

> acme dt -p \\path\to\some\network\drive
Loads the templates from the defined location.

Project creation
================
> acme cp PROJECTNAME
Creates a new project inside the current directory.
A new subfolder named 'PROJECTNAME' will get created. Inside is an empty ACME project.

Module creation
===============
> acme cm MODULENAME
Creates a new modules. The current directory must be inside a valid ACME project. The type of the module is 'static'.

> acme cm MODULENAME -t=exe
Creates a new 'exe' module. An additional main.cpp will get created.

> acme cm MODULENAME -t=dynamic
Creates a new dynamic module.

Module renaming
===============
> acme rm MODULENAME NEWMODULENAME
Renames the module. The current directory must be inside a valid ACME project. The current directory must not be the module directory
of the module which get's renamed.

Module deletion
==============
> acme dm MODULENAME
Deletes the module. The current directory must be inside a valid ACME project. The current directory must not be the module directory
of the module which get's removed.

File creation
=============
> acme cf FILENAME
Creates a new class with source, header and test files. The current directory must be inside a valid ACME module.

> acme cf FILENAME -m=MODULENAME
Creates a new class with source, header and test files. The file is created in the given module. The current directory must be inside a valid ACME project.
 
 The following options apply:
     "-NOTEST" skips the creation of test files
     "-NOSOURCE" skips the creation of source files
     "-NOHEADER" skips the creation of header files
     "-PRIVATE" makes the header file private
     "-t=C" for create C-Files  
     
File renaming
==============
> acme rf FILENAME NEWFILENAME
Renames a file. The current directory must be inside a valid ACME module.

> acme rf FILENAME NEWFILENAME -m=MODULENAME
Renames a file. The file is deleted from the given module. The current directory must be inside a valid ACME project.

File deletion
=============
> acme df FILENAME
Deletes a file. The current directory must be inside a valid ACME module.

> acme df FILENAME -m=MODULENAME
Deletes a file. The file is deleted from the given module. The current directory must be inside a valid ACME project.

> acme df FILENAME -test
Only deletes test files.

> acme df FILENAME -source
Only deletes the source file.

> acme df FILENAME -header -test
Deletes header and test files.

Third Party Software
====================
> acme 3psw -list
Lists all available third party software.

> acme 3psw -list -p=\\path\to\some\network\drive
Lists all available third party software in the given path.

> acme 3psw -download=capu
Downloads the given third party software. The current directory must be inside a valid ACME project.

Viewing unused files
====================
> acme pf
Lists all unused files inside a module or project, that are not references inside the CMakeLists.txt

> acme pf -m=MODULENAME
Lsits all unusued files of the specified module. 

> acme pf -printCommand
Lists all unused files with a copy-ready ACME_ADD_FILE(FILENAME) output.

=====================================================================================================
IDE Integration
=====================================================================================================
Integrate jACME inside Visual Studio to get an even better performance boost. Add the desired jACME
commands as external tool and create toolbar buttons.

Because Visual Studio keeps some handles inside the file system, modules cannot be deleted or renamed.

The following tool configurations are recommended:
(Use 'Promt for arguments' and 'Use Output window')

  * CREATE FILE (A file of the desired module must be selected in the solution explorer)
      Command: PATH_TO_ACME_BAT
      Arguments: "cf "
      Initial directory: ${ItemDir}
       
  * RENAME FILE (Select the file which should get renamed!)
      Command: PATH_TO_ACME_BAT
      Arguments: "rf $(ItemFileName) "
      Initial directory: ${ItemDir}  
           
  * DELETE FILE (Select the file which should get removed. "Promt for arguments" is not necessary)
      Command: PATH_TO_ACME_BAT
      Arguments: "df $(ItemFileName)"
      Initial directory: ${ItemDir}  
      
  * CREATE MODULE
      Command: PATH_TO_ACME_BAT
      Arguments: "cm "
      Initial directory: $(SolutionDir)

=====================================================================================================
Project specific templates
=====================================================================================================
jACME creates file and CMakeLists content based on templates. These templates are located inside the jACME installation folder.
If you want to use project specific templates, create a folder called 'templates' inside the 'cmake' folder of your project.
Place the template files you want to adjust inside this folder. jACME will use these files for the current project only.

=====================================================================================================
Templates
=====================================================================================================
You can use environment variables inside each template file. Each variable can have a '.toUpperCase' or '.toLowerCase' appended,
which adjusts the value of the variable. The value of ${projectName.toLowerCase} would insert the project name in
lower case letters at the specified position.

Some jACME commands may provide additional variables. These are lists in the -help output of each command.
