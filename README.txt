ACME stands for Another CMake Extension.
At the beginning of a CMake based project recurrent task have to be done: project structure setup (headers, sources, tests), getting the test framework working on the target platform, and so on. 

ACME tries to simplify and automate this process.

Features:
-	Consistent project structure for each ACME based project. New developers can orient themselves easily.
-	Simplified management of files per compilation unit. Developer only has to specify the file and the required package (e.g. threads), ACME is searching for headers, sources and tests and links them appropriately
-	Toolchain based build process. All platform specific settings are hold inside a respective toolchain file, ACME builds both the modules and the tests for the respective platform
-	Plugins. ACME’s functionality can be enhanced by external tools specified integrated by a plugin. E.g. doxygen documentation generation, source formatting according with astyle, etc.
