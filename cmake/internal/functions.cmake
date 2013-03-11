#
# Copyright (C) 2012 BMW Car IT GmbH
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Split arguments passed to a function into several lists separated by
# specified identifiers that do not have an associated list e.g.:
#
# SET(arguments
#   hello world
#   LIST3 foo bar
#   LIST1 fuz baz
#   )
# ARGUMENT_SPLITTER("${arguments}" "LIST1 LIST2 LIST3" ARG)
#
# results in 8 distinct variables:
#  * ARG_DEFAULT_FOUND: 1
#  * ARG_DEFAULT: hello;world
#  * ARG_LIST1_FOUND: 1
#  * ARG_LIST1: fuz;baz
#  * ARG_LIST2_FOUND: 0
#  * ARG_LIST2:
#  * ARG_LIST3_FOUND: 1
#  * ARG_LIST3: foo;bar

#--------------------------------------------------------------------------
# Including functions and macros that are used by the functions in functions.cmake
#--------------------------------------------------------------------------

INCLUDE(${ACME_PATH}/internal/targetdefinitions.cmake)
INCLUDE(${ACME_PATH}/internal/doit.cmake)
INCLUDE(${ACME_PATH}/internal/hooks.cmake)

#--------------------------------------------------------------------------
# Internal methods provided by ACME and invoked by acme.cmake
#--------------------------------------------------------------------------

FUNCTION(INTERNAL_ACME_ADD_SUBDIRECTORY int_sub_dir_name)
	MESSAGE(VERBOSE INTERNAL_ACME_ADD_SUBDIRECTORY "adding subdirectory ${int_sub_dir_name}")
	SET(CURRENT_MODULE_NAME "${int_sub_dir_name}")
	ADD_SUBDIRECTORY("${int_sub_dir_name}")
	INTERNAL_JUST_DOIT()
ENDFUNCTION(INTERNAL_ACME_ADD_SUBDIRECTORY)


FUNCTION(INTERNAL_ADD_CMAKE_PROJECT iaap_name)
	MESSAGE(VERBOSE INTERNAL_ADD_CMAKE_PROJECT "adding cmake project ${iaap_name}")
	MESSAGE(VERBOSE "Configuring build for external cmake project ${iaap_name}")

	IF(NOT DEFINED THIRD_PARTY_DIR)
		SET(THIRD_PARTY_DIR ${PROJECT_SOURCE_DIR}/3psw)
	ENDIF()
	SET(GLOBAL_CMAKE_PROJECTS ${GLOBAL_CMAKE_PROJECTS} "${iaap_name}" CACHE INTERNAL "global list of all added cmake project")
	

	INTERNAL_ARGUMENT_SPLITTER("${ARGN}" "LIBNAMES LIBDIRS INCLUDE_DIRS BINARY_INCLUDE_DIRS ABSOLUTE_INCLUDE_DIRS URL CHECKSUM SOURCE_DIR CMAKE_ARGUMENTS DEPENDENT_DEFINITIONS DEPENDENT_DEBUG_DEFINITIONS DEPENDENT_RELEASE_DEFINITIONS INSTALL CONFIG_FILE REQUIRED_PACKAGES USE_LIBRARY_OUTPUT_DIRECTORY USE_RUNTIME_OUTPUT_DIRECTORY" IAAP )

	IF(NOT "${IAAP_URL}" STREQUAL "")
		SET(iaap_method DOWNLOAD_DIR "${THIRD_PARTY_DIR}/${iaap_name}"
						URL          "${IAAP_URL}"
						URL_MD5      "${IAAP_CHECKSUM}")
		MESSAGE(VERBOSE INTERNAL_ADD_CMAKE_PROJECT "Using download method for external project with url ${IAAP_URL}")
	ELSEIF(NOT "${IAAP_SOURCE_DIR}" STREQUAL "")
		INTERNAL_LIST_TO_STRING("${IAAP_SOURCE_DIR}" IAAP_CONVERTED_SOURCE_DIR)  
		SET(IAAP_SOURCE_DIR "${IAAP_CONVERTED_SOURCE_DIR}")
		SET(iaap_method SOURCE_DIR  "${IAAP_SOURCE_DIR}"
								     DOWNLOAD_COMMAND "")
		MESSAGE(VERBOSE INTERNAL_ADD_CMAKE_PROJECT "Using source directory for external project: ${IAAP_SOURCE_DIR}")
	ELSEIF(EXISTS "${THIRD_PARTY_DIR}/${iaap_name}")
		SET(IAAP_SOURCE_DIR "${THIRD_PARTY_DIR}/${iaap_name}")
		SET(iaap_method SOURCE_DIR "${IAAP_SOURCE_DIR}"
		                           DOWNLOAD_COMMAND "")	        
		MESSAGE(VERBOSE INTERNAL_ADD_CMAKE_PROJECT "Using project-local third party dir source directory for external project: ${IAAP_SOURCE_DIR}")
	ENDIF()
	
    # Adds the packages that are required by the external cmake project to the cache variable "GLOBAL_CMAKE_PROJECTS_REQUIRED_PACKAGES"
    IF(NOT "${IAAP_REQUIRED_PACKAGES}" STREQUAL "")
	    SET(CURRENT_MODULE_NAME "${iaap_name}")
	    SET(${iaap_name}_PACKAGES "" CACHE INTERNAL "")
	    FOREACH(package ${IAAP_REQUIRED_PACKAGES})
			INTERNAL_ADD_DEPENDENCY("${package}")
	    ENDFOREACH()
	    SET(GLOBAL_CMAKE_PROJECTS_REQUIRED_PACKAGES ${GLOBAL_CMAKE_PROJECTS_REQUIRED_PACKAGES} "${iaap_name}_PACKAGES" CACHE INTERNAL "global list of all required packages that are used by a specific external cmake project")   
	ENDIF()
	

	IF(NOT "${iaap_method}" STREQUAL "")
		SET(BUILD_${iaap_name} 1 CACHE BOOL "Use ${iaap_name}")
		SET(GLOBAL_BUILD_PROJECT ${GLOBAL_BUILD_PROJECT} BUILD_${iaap_name} CACHE INTERNAL "global list of all build variables of the added cmake projekts")
		IF(${BUILD_${iaap_name}}) 
			MESSAGE(VERBOSE "Going to build ${iaap_name}")
			SET(ilts_cmake_arguments "")
			FOREACH(ilts_cmake_argument ${IAAP_CMAKE_ARGUMENTS})
				SET(ilts_cmake_arguments ${ilts_cmake_arguments} -D${ilts_cmake_argument})
			ENDFOREACH()
			SET(ilts_install_command INSTALL_COMMAND "")
			IF("${IAAP_INSTALL}" EQUAL 1) 
				SET(ilts_install_command "")
			ENDIF()
			
			
			IF("${TARGET_OS}" STREQUAL "Windows")
				SET(BUILD_TYPE "")
			ELSE()
				SET(BUILD_TYPE /${CMAKE_BUILD_TYPE})
			ENDIF()

			IF("${IAAP_USE_LIBRARY_OUTPUT_DIRECTORY}" STREQUAL "")
				SET(USE_LIBRARY_OUTPUT_DIRECTORY "${CMAKE_INSTALL_PREFIX}/${iaap_name}_${PROJECT_NAME}/lib/${TARGET_OS}_${TARGET_ARCH}${BUILD_TYPE}")
			ELSE()
				SET(USE_LIBRARY_OUTPUT_DIRECTORY "${IAAP_USE_LIBRARY_OUTPUT_DIRECTORY}")
				MESSAGE(VERBOSE "Using custom library output dir: ${USE_LIBRARY_OUTPUT_DIRECTORY}")
			ENDIF()

            IF("${IAAP_USE_RUNTIME_OUTPUT_DIRECTORY}" STREQUAL "")
                SET(USE_RUNTIME_OUTPUT_DIRECTORY "${CMAKE_INSTALL_PREFIX}/${iaap_name}_${PROJECT_NAME}/bin/${TARGET_OS}_${TARGET_ARCH}${BUILD_TYPE}")
			ELSE()
                SET(USE_RUNTIME_OUTPUT_DIRECTORY "${IAAP_USE_RUNTIME_OUTPUT_DIRECTORY}")
			ENDIF()
			MESSAGE(VERBOSE "Creating cmake external library for ${iaap_name}")
			ExternalProject_Add(
			  ${iaap_name}
			  PREFIX ${GLOBAL_TOP_LEVEL_BINARY_DIR}/${iaap_name}_${PROJECT_NAME}
			  #PREFIX ${CMAKE_BINARY_DIR}/${iaap_name}
			  ${iaap_method}
			  UPDATE_COMMAND ""
			  CMAKE_ARGS -DCMAKE_TOOLCHAIN_FILE:PATH=${CMAKE_TOOLCHAIN_FILE}
						 -DCMAKE_CXX_FLAGS_RELEASE:STRING=${CMAKE_CXX_FLAGS_RELEASE}
						 -DCMAKE_C_FLAGS_RELEASE:STRING=${CMAKE_C_FLAGS_RELEASE}
						 -DCMAKE_CXX_FLAGS_DEBUG:STRING=${CMAKE_CXX_FLAGS_DEBUG}
						 -DCMAKE_C_FLAGS_DEBUG:STRING=${CMAKE_C_FLAGS_DEBUG}
						 -DCMAKE_CXX_FLAGS:STRING=${CMAKE_CXX_FLAGS}
						 -DCMAKE_C_FLAGS:STRING=${CMAKE_C_FLAGS}
						 -DCMAKE_BUILD_TYPE:STRING=${CMAKE_BUILD_TYPE}
						 -DCONFIG_BUILD_UNITTESTS:BOOLEAN=${CONFIG_BUILD_UNITTESTS}
						 -DCMAKE_ARCHIVE_OUTPUT_DIRECTORY:PATH=${USE_LIBRARY_OUTPUT_DIRECTORY}
						 -DCMAKE_LIBRARY_OUTPUT_DIRECTORY:PATH=${USE_LIBRARY_OUTPUT_DIRECTORY}
						 -DCMAKE_RUNTIME_OUTPUT_DIRECTORY:PATH=${USE_RUNTIME_OUTPUT_DIRECTORY}
						 -DCMAKE_DOC_OUTPUT_DIRECTORY:PATH=${CMAKE_INSTALL_PREFIX}/doc
						#-DCMAKE_ARCHIVE_OUTPUT_DIRECTORY:PATH=${THIRD_PARTY_DIR}/deliverable/${iaap_name}/lib/${TARGET_OS}_${TARGET_ARCH}
						#-DCMAKE_LIBRARY_OUTPUT_DIRECTORY:PATH=${THIRD_PARTY_DIR}/deliverable/${iaap_name}/lib/${TARGET_OS}_${TARGET_ARCH}
						#-DCMAKE_INSTALL_PREFIX=${THIRD_PARTY_DIR}/deliverable/${iaap_name}
						 -DCMAKE_INSTALL_PREFIX:PATH=${CMAKE_INSTALL_PREFIX}/${iaap_name}_${PROJECT_NAME}
						 -DCONFIG_BUILD_GLOBAL_TEST_EXECUTABLE:BOOLEAN=${CONFIG_BUILD_GLOBAL_TEST_EXECUTABLE}
						 -DGLOBAL_TOP_LEVEL_SOURCE_DIR:PATH=${GLOBAL_TOP_LEVEL_SOURCE_DIR}
						 -DGLOBAL_TOP_LEVEL_BINARY_DIR:PATH=${GLOBAL_TOP_LEVEL_BINARY_DIR}
						 -DCONFIG_CREATE_DEPENDENCY_GRAPH:BOOL=${CONFIG_CREATE_DEPENDENCY_GRAPH}
						 -DCONFIG_CREATE_BUILD_REPORT:BOOL=${CONFIG_CREATE_BUILD_REPORT}
						 ${ilts_cmake_arguments}
						 "${ilts_install_command}"
			)

#			SET(${iaap_name}_DIR "${CMAKE_BINARY_DIR}/${iaap_name}")
			SET(${iaap_name}_DIR "${IAAP_SOURCE_DIR}")
			
			SET(iacp_include_dirs "${CMAKE_INSTALL_PREFIX}/${iaap_name}_${PROJECT_NAME}/include")
			IF(NOT ${IAAP_URL} STREQUAL "")
				FOREACH(iaap_include_dir ${IAAP_INCLUDE_DIRS})
					SET(iacp_include_dirs "${iacp_include_dirs}" "${GLOBAL_TOP_LEVEL_BINARY_DIR}/${iaap_name}_${PROJECT_NAME}/${iaap_include_dir}")
				ENDFOREACH()
			ELSE()

			FOREACH(iaap_include_dir ${IAAP_INCLUDE_DIRS})
				SET(iacp_include_dirs "${iacp_include_dirs}" "${CMAKE_INSTALL_PREFIX}/${iaap_name}_${PROJECT_NAME}/include/${iaap_include_dir}")
				#SET(${iaap_name}_INCLUDE_DIR "${${iaap_name}_INCLUDE_DIR}" "${CMAKE_INSTALL_PREFIX}/include/${iaap_include_dir}")
			ENDFOREACH()
			
			FOREACH(iaap_include_dir ${IAAP_BINARY_INCLUDE_DIRS})
				SET(iacp_include_dirs "${iacp_include_dirs}" "${GLOBAL_TOP_LEVEL_BINARY_DIR}/${iaap_name}_${PROJECT_NAME}/src/${iaap_name}-build/${iaap_include_dir}")
			endforeach()
			
			FOREACH(iaap_include_dir ${IAAP_ABSOLUTE_INCLUDE_DIRS})
				SET(iacp_include_dirs "${iacp_include_dirs}" "${iaap_include_dir}")
			ENDFOREACH()	
		ELSE()
			MESSAGE(VERBOSE "Not going to build ${iaap_name}")
		ENDIF()

		INTERNAL_ADD_EXTERNAL_LIBRARY( ${iaap_name} 
							         INCLUDE_DIRS                  "${iacp_include_dirs}"
						             LIBRARY_DIRS                  "${USE_LIBRARY_OUTPUT_DIRECTORY}" ${IAAP_LIBDIRS}
							         LIBNAMES                      "${IAAP_LIBNAMES}"
							         DEPENDENT_DEFINITIONS         "${IAAP_DEPENDENT_DEFINITIONS}"
									 DEPENDENT_DEBUG_DEFINITIONS   "${IAAP_DEPENDENT_DEBUG_DEFINITIONS}"
									 DEPENDENT_RELEASE_DEFINITIONS "${IAAP_DEPENDENT_RELEASE_DEFINITIONS}")
								
		ENDIF()
	ENDIF()
ENDFUNCTION(INTERNAL_ADD_CMAKE_PROJECT)


FUNCTION(INTERNAL_ADD_EXTERNAL_LIBRARY ial_name)
	MESSAGE(VERBOSE INTERNAL_ADD_EXTERNAL_LIBRARY "Adding library ${ial_name}")
	INTERNAL_ARGUMENT_SPLITTER("${ARGN}" "INCLUDE_DIRS LIBRARY_DIRS LIBNAMES DEPENDENT_DEFINITIONS DEPENDENT_DEBUG_DEFINITIONS DEPENDENT_RELEASE_DEFINITIONS" IAL)

	SET(GLOBAL_CMAKE_PROJECTS ${GLOBAL_CMAKE_PROJECTS} "${ial_name}" CACHE INTERNAL "global list of all added cmake project")
	SET(${ial_name}_INCLUDE_DIRS "" CACHE INTERNAL "")
	FOREACH(ial_include_dir ${IAL_INCLUDE_DIRS})
		SET(${ial_name}_INCLUDE_DIRS ${${ial_name}_INCLUDE_DIRS} "${ial_include_dir}" CACHE INTERNAL "")
	ENDFOREACH()

	SET(${ial_name}_LIBRARIES_DIR "" CACHE INTERNAL "")
	FOREACH(ial_library_dir ${IAL_LIBRARY_DIRS})
		SET(${ial_name}_LIBRARIES_DIR ${${ial_name}_LIBRARIES_DIR} "${ial_library_dir}" CACHE INTERNAL "")
	ENDFOREACH()
			
	SEPARATE_ARGUMENTS(IAL_LIBNAMES)
	SET(${ial_name}_LIBRARIES ${IAL_LIBNAMES} CACHE INTERNAL "")
	SET(${ial_name}_DEPENDENT_DEBUG_DEFINITIONS "${IAL_DEPENDENT_DEFINITIONS}" "${IAL_DEPENDENT_DEBUG_DEFINITIONS}" CACHE INTERNAL "")
	SET(${ial_name}_DEPENDENT_RELEASE_DEFINITIONS "${IAL_DEPENDENT_DEFINITIONS}" "${IAL_DEPENDENT_RELEASE_DEFINITIONS}" CACHE INTERNAL "")
	SET(${ial_name}_FOUND 1 CACHE INTERNAL "")
	SET(${ial_name}_INTERNAL 1 CACHE INTERNAL "")
	
	MARK_AS_ADVANCED(								
		${ial_name}_INCLUDE_DIRS
		${ial_name}_LIBRARIES_DIR
		${ial_name}_LIBRARIES
		${ial_name}_DEPENDENT_DEBUG_DEFINITIONS
		${ial_name}_DEPENDENT_RELEASE_DEFINITIONS
		${ial_name}_FOUND
		${ial_name}_INTERNAL
	)

	SET(GLOBAL_EXTERNAL_LIBRARY_INCLUDE_DIR						${GLOBAL_EXTERNAL_LIBRARY_INCLUDE_DIR} 						${${ial_name}_INCLUDE_DIRS}						CACHE INTERNAL "global list of all variables which indicate the include directory of external libraries")
	SET(GLOBAL_EXTERNAL_LIBRARY_LIBRARIES_DIR					${GLOBAL_EXTERNAL_LIBRARY_LIBRARIES_DIR}					${${ial_name}_LIBRARIES_DIR}					CACHE INTERNAL "global list of all variables which indicate the library directory of external libraries")
	SET(GLOBAL_EXTERNAL_LIBRARY_LIBRARIES						${GLOBAL_EXTERNAL_LIBRARY_LIBRARIES}						${${ial_name}_LIBRARIES} 						CACHE INTERNAL "global list of all variables which indicate the libraries of external libraries")

	LIST(REMOVE_ITEM "${ial_name}_DEPENDENT_DEBUG_DEFINITIONS" "")
	LIST(LENGTH "${ial_name}_DEPENDENT_DEBUG_DEFINITIONS" list_length)
	IF(${list_length})
		SET(GLOBAL_EXTERNAL_LIBRARY_DEPENDENT_DEBUG_DEFINITIONS		${GLOBAL_EXTERNAL_LIBRARY_DEPENDENT_DEBUG_DEFINITIONS} 		${ial_name}_DEPENDENT_DEBUG_DEFINITIONS			CACHE INTERNAL "global list of all variables which indicate the dependent debug definitions of external libraries")
	ENDIF()
	
	LIST(REMOVE_ITEM "${ial_name}_DEPENDENT_RELEASE_DEFINITIONS" "")
	LIST(LENGTH "${ial_name}_DEPENDENT_RELEASE_DEFINITIONS" list_length)
	IF(${list_length})
		SET(GLOBAL_EXTERNAL_LIBRARY_DEPENDENT_RELEASE_DEFINITIONS	${GLOBAL_EXTERNAL_LIBRARY_DEPENDENT_RELEASE_DEFINITIONS}	${ial_name}_DEPENDENT_RELEASE_DEFINITIONS	 	CACHE INTERNAL "global list of all variables which indicate the dependent release definitions of external libraries")
	ENDIF()
	
	
	LIST(REMOVE_ITEM "${ial_name}_FOUND" "")
	LIST(LENGTH "${ial_name}_FOUND" list_length)
	IF(${list_length})
		SET(GLOBAL_EXTERNAL_LIBRARY_FOUND		${GLOBAL_EXTERNAL_LIBRARY_FOUND}	${ial_name}_FOUND		CACHE INTERNAL "global list of all variables which indicate if an external library was found")
	ENDIF()
	
	LIST(REMOVE_ITEM "${ial_name}_INTERNAL" "")
	LIST(LENGTH "${ial_name}_INTERNAL" list_length)
	IF(${list_length})
		SET(GLOBAL_EXTERNAL_LIBRARY_INTERNAL	${GLOBAL_EXTERNAL_LIBRARY_INTERNAL}	${ial_name}_INTERNAL	CACHE INTERNAL "")
	ENDIF()
	
	#SET(GLOBAL_INCLUDE_DIRECTORIES ${GLOBAL_INCLUDE_DIRECTORIES} ${${ial_name}_INCLUDE_DIR}   CACHE INTERNAL "collect include directories")
	#SET(GLOBAL_LIB_DIRECTORIES     ${GLOBAL_LIB_DIRECTORIES}     ${${ial_name}_LIBRARIES_DIR} CACHE INTERNAL "collect lib directories")
	#SET(GLOBAL_LIBRARIES           ${GLOBAL_LIBRARIES}           ${${ial_name}_LIBRARIES}     CACHE INTERNAL "collect all linkable libraries")

	#message("include dirs ${${ial_name}_INCLUDE_DIR}")
	#message("library dirs ${${ial_name}_LIBRARIES_DIR}")
	#message("libraries ${${ial_name}_LIBRARIES}")
	#message("depandent defs ${${ial_name}_DEPENDENT_DEFINITIONS}")
		
ENDFUNCTION(INTERNAL_ADD_EXTERNAL_LIBRARY)


FUNCTION(INTERNAL_ADD_MODULE_INTERNAL ami_module_name ami_type)
	MESSAGE(VERBOSE  "---------------------------------------------------------------------------")
	MESSAGE(VERBOSE  "Configuring build for ${ami_module_name} (${ami_type})")
	
	STRING(TOUPPER ${ami_type} CURRENT_MODULE_TYPE)
	IF(NOT "${CURRENT_MODULE_NAME}" STREQUAL "")
		STRING(TOUPPER ${CURRENT_MODULE_NAME} CURRENT_UPPER_MODULE_NAME)
	ENDIF()
	#SET(CURRENT_MODULE_NAME "${ami_module_name}")
	
	
	SET(${CURRENT_MODULE_NAME}_HAS_SOURCE_FILES 0 CACHE INTERNAL "")
	SET(${CURRENT_MODULE_NAME}_MODULE_TYPE "${CURRENT_MODULE_TYPE}" CACHE INTERNAL "")
	SET(${CURRENT_MODULE_NAME}_DIR "${CMAKE_CURRENT_SOURCE_DIR}" CACHE INTERNAL "")
	SET(${CURRENT_MODULE_NAME}_BUILD_ENABLED 		  1	 CACHE INTERNAL "")
	#SET(${CURRENT_MODULE_NAME}_COMPILE_FLAGS          "" CACHE INTERNAL "")
	SET(${CURRENT_MODULE_NAME}_DEBUG_COMPILER_FLAGS   "" CACHE INTERNAL "")
	SET(${CURRENT_MODULE_NAME}_RELEASE_COMPILER_FLAGS "" CACHE INTERNAL "")
	#SET(${CURRENT_MODULE_NAME}_LINKER_FLAGS           "" CACHE INTERNAL "")
	SET(${CURRENT_MODULE_NAME}_DEBUG_LINKER_FLAGS     "" CACHE INTERNAL "")
	SET(${CURRENT_MODULE_NAME}_RELEASE_LINKER_FLAGS   "" CACHE INTERNAL "")
	SET(${CURRENT_MODULE_NAME}_DEBUG_DEFINITIONS      "" CACHE INTERNAL "")
    SET(${CURRENT_MODULE_NAME}_RELEASE_DEFINITIONS    "" CACHE INTERNAL "")
	SET(${CURRENT_MODULE_NAME}_DEPENDENCIES           "" CACHE INTERNAL "")
	SET(${CURRENT_MODULE_NAME}_LIBRARIES              "" CACHE INTERNAL "")
	SET(${CURRENT_MODULE_NAME}_INSTALL_FILES          "" CACHE INTERNAL "")
    SET(${CURRENT_MODULE_NAME}_PACKAGE_LIBS           "" CACHE INTERNAL "")
	SET(${CURRENT_MODULE_NAME}_INCLUDE_DIRS           "" CACHE INTERNAL "")
	SET(${CURRENT_MODULE_NAME}_FOUND                  1  CACHE INTERNAL "")
	SET(${CURRENT_MODULE_NAME}_TEST_FILES			  "" CACHE INTERNAL "")
	SET(${CURRENT_UPPER_MODULE_NAME}_MODULE_SOURCE_FILES "" CACHE INTERNAL "")
	SET(${CURRENT_MODULE_NAME}_DEPENDENCY_EDGES		  "" CACHE INTERNAL "")
	SET(${CURRENT_MODULE_NAME}_SOURCE_GROUPS		  "" CACHE INTERNAL "")

	SET(GLOBAL_MODULE_NAMES						${GLOBAL_MODULE_NAMES}						${CURRENT_MODULE_NAME}							CACHE INTERNAL "global list of all module names")
	SET(GLOBAL_MODULE_HAS_SOURCE_FILES 			${GLOBAL_MODULE_HAS_SOURCE_FILES} 			${CURRENT_MODULE_NAME}_HAS_SOURCE_FILES 		CACHE INTERNAL "global list of all variables which indentifiy if the specific module has source files")
	SET(GLOBAL_MODULE_TYPE 						${GLOBAL_MODULE_TYPE}  						${CURRENT_MODULE_NAME}_MODULE_TYPE 				CACHE INTERNAL "global list of all variables which store the type of the specific module")
	SET(GLOBAL_MODULE_DIR 						${GLOBAL_MODULE_DIR} 						${CURRENT_MODULE_NAME}_DIR 						CACHE INTERNAL "global list of all variables which store the directory of the specific module")
	SET(GLOBAL_MODULE_BUILD_ENABLED 			${GLOBAL_MODULE_BUILD_ENABLED} 				${CURRENT_MODULE_NAME}_BUILD_ENABLED 			CACHE INTERNAL "global list of all variables which show if build is for the specific module enabled")
	#SET(GLOBAL_MODULE_COMPILE_FLAGS 			${GLOBAL_MODULE_COMPILE_FLAGS} 				${CURRENT_MODULE_NAME}_COMPILE_FLAGS			CACHE INTERNAL "global list of all variables which store the compile flags for the specific module")
	#SET(GLOBAL_MODULE_DEBUG_COMPILER_FLAGS 	${GLOBAL_MODULE_DEBUG_COMPILER_FLAGS} 		${CURRENT_MODULE_NAME}_DEBUG_COMPILER_FLAGS		CACHE INTERNAL "global list of all variables which store the debug compiler flags for the specific module")
	#SET(GLOBAL_MODULE_RELEASE_COMPILER_FLAGS 	${GLOBAL_MODULE_RELEASE_COMPILER_FLAGS} 	${CURRENT_MODULE_NAME}_RELEASE_COMPILER_FLAGS 	CACHE INTERNAL "global list of all variables which store the release compiler flags for the specific module")
	#SET(GLOBAL_MODULE_LINKER_FLAGS 			${GLOBAL_MODULE_LINKER_FLAGS} 				${CURRENT_MODULE_NAME}_LINKER_FLAGS 			CACHE INTERNAL "global list of all variables which store the linker flags for the specific module")
	#SET(GLOBAL_MODULE_DEBUG_LINKER_FLAGS 		${GLOBAL_MODULE_DEBUG_LINKER_FLAGS} 		${CURRENT_MODULE_NAME}_DEBUG_LINKER_FLAGS 		CACHE INTERNAL "global list of all variables which store the debug linker flags for the specific module")
	#SET(GLOBAL_MODULE_RELEASE_LINKER_FLAGS 	${GLOBAL_MODULE_RELEASE_LINKER_FLAGS} 		${CURRENT_MODULE_NAME}_RELEASE_LINKER_FLAGS		CACHE INTERNAL "global list of all variables which store the release linker flags for the specific module")
	#SET(GLOBAL_MODULE_DEBUG_DEFINITIONS 		${GLOBAL_MODULE_DEBUG_DEFINITIONS} 			${CURRENT_MODULE_NAME}_DEBUG_DEFINITIONS  		CACHE INTERNAL "global list of all variables which store the debug definitions for the specific module")
	#SET(GLOBAL_MODULE_RELEASE_DEFINITIONS 		${GLOBAL_MODULE_RELEASE_DEFINITIONS} 		${CURRENT_MODULE_NAME}_RELEASE_DEFINITIONS 		CACHE INTERNAL "global list of all variables which store the release definitions for the specific module")
	#SET(GLOBAL_MODULE_DEPENDENCIES 			${GLOBAL_MODULE_DEPENDENCIES} 				${CURRENT_MODULE_NAME}_DEPENDENCIES 			CACHE INTERNAL "global list of all variables which store the dependencies of the specific module")
	#SET(GLOBAL_MODULE_LIBRARIES 				${GLOBAL_MODULE_LIBRARIES} 					${CURRENT_MODULE_NAME}_LIBRARIES 				CACHE INTERNAL "global list of all variables which store the libraries of the specific module")	
	#SET(GLOBAL_MODULE_INSTALL_FILES 			${GLOBAL_MODULE_INSTALL_FILES} 				${CURRENT_MODULE_NAME}_INSTALL_FILES 			CACHE INTERNAL "global list of all variables which identificate the install files for the specific module")
	#SET(GLOBAL_MODULE_PACKAGE_LIBS 			${GLOBAL_MODULE_PACKAGE_LIBS} 				${CURRENT_MODULE_NAME}_PACKAGE_LIBS 			CACHE INTERNAL "global list of all variables which identificate the package libaries for the specific module")
	SET(GLOBAL_MODULE_FOUND 					${GLOBAL_MODULE_FOUND} 						${CURRENT_MODULE_NAME}_FOUND					CACHE INTERNAL "global list of all variables which show that a specific module was found")
	#SET(GLOBAL_MODULE_TEST_FILES				${GLOBAL_MODULE_TEST_FILES}					${CURRENT_MODULE_NAME}_TEST_FILES				CACHE INTERNAL "global list of all variables which store the test files of the specific module")
	#SET(GLOBAL_MODULE_SOURCE_FILES				${GLOBAL_MODULE_SOURCE_FILES}				${CURRENT_UPPER_MODULE_NAME}_MODULE_SOURCE_FILES CACHE INTERNAL "global list of all variables which store the source files of the specific module")
	#SET(GLOBAL_MODULE_OPTIONAL_FILES 			${GLOBAL_MODULE_OPTIONAL_FILES}				${aof_prefix}_ENABLE 							 CACHE INTERNAL "global list of variables which indicate if an optional file ist enabled or not")

    #IF(CONFIG_CREATE_TEST_COVERAGE)
       #SET(${CURRENT_MODULE_NAME}_DEBUG_COMPILER_FLAGS  ${${CURRENT_MODULE_NAME}_DEBUG_COMPILER_FLAGS} "-fprofile-arcs" "-ftest-coverage" CACHE INTERNAL "")
       #SET(${CURRENT_MODULE_NAME}_RELEASE_COMPILER_FLAGS  ${${CURRENT_MODULE_NAME}_RELEASE_COMPILER_FLAGS} "-fprofile-arcs" "-ftest-coverage" CACHE INTERNAL "")
       #SET(${CURRENT_MODULE_NAME}_DEBUG_LINKER_FLAGS ${${CURRENT_MODULE_NAME}_DEBUG_LINKER_FLAGS} "-fprofile-arcs" CACHE INTERNAL "")    
       #SET(${CURRENT_MODULE_NAME}_RELEASE_LINKER_FLAGS ${${CURRENT_MODULE_NAME}_RELEASE_LINKER_FLAGS} "-fprofile-arcs" CACHE INTERNAL "")    
    #ENDIF()
ENDFUNCTION(INTERNAL_ADD_MODULE_INTERNAL)


FUNCTION(INTERNAL_ADD_MODULE ad_module_name ad_type)
	INTERNAL_ADD_MODULE_INTERNAL(${ad_module_name} ${ad_type})
	STRING(TOUPPER ${ad_module_name} CURRENT_UPPER_MODULE_NAME)
	SET(WITH_${CURRENT_UPPER_MODULE_NAME} 1 CACHE INTERNAL "Use module ${CURRENT_UPPER_MODULE_NAME}")
	SET(GLOBAL_WITH_MODULE ${GLOBAL_WITH_MODULE} WITH_${CURRENT_UPPER_MODULE_NAME} CACHE INTERNAL "global list of all used modules")
	
	IF("${ad_type}" STREQUAL "static")
	SET(GLOBAL_UTILS_MODULES_STATIC ${GLOBAL_UTILS_MODULES_STATIC} 		"${ad_module_name}" CACHE INTERNAL "stores the module names of all static modules")	
	ENDIF()
	
	IF("${ad_type}" STREQUAL "dynamic")
	SET(GLOBAL_UTILS_MODULES_DYNAMIC ${GLOBAL_UTILS_MODULES_DYNAMIC}	"${ad_module_name}" CACHE INTERNAL "stores the module names of all dynamic modules")		
	ENDIF()
	
	IF("${ad_type}" STREQUAL "exe")
	SET(GLOBAL_UTILS_MODULES_EXE ${GLOBAL_UTILS_MODULES_EXE}     		"${ad_module_name}" CACHE INTERNAL "stores the module names of all exe modules")	
	ENDIF()
ENDFUNCTION(INTERNAL_ADD_MODULE)


FUNCTION(INTERNAL_ADD_OPTIONAL_MODULE aom_module_name aom_type)
	INTERNAL_ADD_MODULE_INTERNAL(${aom_module_name} ${aom_type})
	STRING(TOUPPER ${aom_module_name} CURRENT_UPPER_MODULE_NAME)
	SET(WITH_${CURRENT_UPPER_MODULE_NAME} 1 CACHE BOOL "Use module ${CURRENT_UPPER_MODULE_NAME}")
	SET(GLOBAL_WITH_MODULE ${GLOBAL_WITH_MODULE} WITH_${CURRENT_UPPER_MODULE_NAME} CACHE INTERNAL "global list of all used modules")
ENDFUNCTION(INTERNAL_ADD_OPTIONAL_MODULE)


FUNCTION(INTERNAL_TRY_TO_SATISFY_DEPENDENCY_USING_FIND_PACKAGE dep_name)
	MESSAGE(VERBOSE  "${CURRENT_MODULE_NAME} is trying to find ${dep_name}")

	IF(NOT "${dep_name}_FOUND" )
		SET(CMAKE_MODULE_PATH_DEFAULT ${CMAKE_MODULE_PATH})
		SET(CMAKE_MODULE_PATH "${CMAKE_SOURCE_DIR}/cmake/modules")
		
		UNSET(${dep_name}_INCLUDE_DIRS)
		
		find_package(${dep_name} QUIET)
		
		
		SET(CMAKE_MODULE_PATH "${CMAKE_MODULE_PATH_DEFAULT}")

		IF(NOT "${dep_name}_FOUND")
			find_package(${dep_name} QUIET)
		ENDIF()

		IF("${dep_name}_FOUND")
			MARK_AS_ADVANCED(
						 ${dep_name}_INCLUDE_DIRS
						 ${dep_name}_LIBRARIES
						 ${dep_name}_LIBRARY_DIRS
						)
		ENDIF()
	ENDIF()

    IF("${dep_name}_FOUND")

		SET(${CURRENT_MODULE_NAME}_INCLUDE_DIRS ${${CURRENT_MODULE_NAME}_INCLUDE_DIRS} "${${dep_name}_INCLUDE_DIRS}" CACHE INTERNAL "")
		SET(${CURRENT_MODULE_NAME}_PACKAGE_LIBS ${${CURRENT_MODULE_NAME}_PACKAGE_LIBS} ${${dep_name}_LIBRARIES} CACHE INTERNAL "")
		SET(${CURRENT_MODULE_NAME}_PACKAGE_LIB_DIRS ${${CURRENT_MODULE_NAME}_PACKAGE_LIB_DIRS} ${${dep_name}_LIBRARY_DIRS} CACHE INTERNAL "")
	
	ELSE()
		MESSAGE(VERBOSE "Could not satisfy required dependency '${dep_name}'.")
	ENDIF()

	SET(${dep_name}_FOUND 			${${dep_name}_FOUND}			CACHE INTERNAL "")
	SET(${dep_name}_INCLUDE_DIRS	${${dep_name}_INCLUDE_DIRS}		CACHE INTERNAL "")
	SET(${dep_name}_LIBRARIES		${${dep_name}_LIBRARIES}		CACHE INTERNAL "")	
	SET(${dep_name}_LIBRARY_DIRS	${${dep_name}_LIBRARY_DIRS}		CACHE INTERNAL "")

	LIST(REMOVE_ITEM "${dep_name}_FOUND" "")
	LIST(LENGTH "${dep_name}_FOUND" list_length)
	IF(${list_length})
	SET(GLOBAL_PACKAGE_FOUND			${GLOBAL_PACKAGE_FOUND}			"${dep_name}_FOUND" 		CACHE INTERNAL	"global list of all variables which identificate the found packages")
	ENDIF()
	
	LIST(REMOVE_ITEM "${dep_name}_INCLUDE_DIRS" "")
	LIST(LENGTH "${dep_name}_INCLUDE_DIRS" list_length)
	IF(${list_length})
	SET(GLOBAL_PACKAGE_INCLUDE_DIRS		${GLOBAL_PACKAGE_INCLUDE_DIRS} 	"${dep_name}_INCLUDE_DIRS"	CACHE INTERNAL	"global list of all variables which store the include directories of the found packages")
	ENDIF()
	
	LIST(REMOVE_ITEM "${dep_name}_LIBRARIES" "")
	LIST(LENGTH "${dep_name}_LIBRARIES" list_length)
	IF(${list_length})
	SET(GLOBAL_PACKAGE_LIBRARIES		${GLOBAL_PACKAGE_LIBRARIES}		"${dep_name}_LIBRARIES"		CACHE INTERNAL	"global list of all variables which store the libraries of the found packages")
	ENDIF()

	LIST(REMOVE_ITEM "${dep_name}_LIBRARY_DIRS" "")
	LIST(LENGTH "${dep_name}_LIBRARY_DIRS" list_length)
	IF(${list_length})
	SET(GLOBAL_PACKAGE_LIBRARY_DIRS		${GLOBAL_PACKAGE_LIBRARY_DIRS}		"${dep_name}_LIBRARY_DIRS"		CACHE INTERNAL	"global list of all variables which store the library dirs of the found packages")
	ENDIF()
ENDFUNCTION(INTERNAL_TRY_TO_SATISFY_DEPENDENCY_USING_FIND_PACKAGE)


FUNCTION(INTERNAL_OPTIONAL_PACKAGE pkg_name)
	MESSAGE(VERBOSE "${CURRENT_MODULE_NAME} optionally requires package ${pkg_name}")

	SET(CMAKE_MODULE_PATH ${CMAKE_SOURCE_DIR}/cmake/modules/)
	
	find_package("${pkg_name}" REQUIRED)
	
	IF(${pkg_name}_FOUND)
		INCLUDE_DIRECTORIES(${${pkg_name}_INCLUDE_DIR})
		SET(${CURRENT_MODULE_NAME}_PACKAGE_LIBS ${${CURRENT_MODULE_NAME}_PACKAGE_LIBS} ${${pkg_name}_LIBRARIES} CACHE INTERNAL "")
		
		LIST(REMOVE_ITEM "${CURRENT_MODULE_NAME}_PACKAGE_LIBS" "")
		LIST(LENGTH "${CURRENT_MODULE_NAME}_PACKAGE_LIBS" list_length)
		IF(${list_length})
			SET(GLOBAL_MODULE_PACKAGE_LIBS 	${GLOBAL_MODULE_PACKAGE_LIBS} 	${CURRENT_MODULE_NAME}_PACKAGE_LIBS 	CACHE INTERNAL "global list of all variables which identificate the package libaries for the specific module")
		ENDIF()
	
	ELSE()
		MESSAGE("Optional package '${pkg_name}' was not found.")
	ENDIF(${pkg_name}_FOUND)
	
	SET(${pkg_name}_FOUND 			${${pkg_name}_FOUND}			CACHE INTERNAL "")
	SET(${pkg_name}_INCLUDE_DIRS	${${pkg_name}_INCLUDE_DIRS}		CACHE INTERNAL "")
	SET(${pkg_name}_LIBRARIES		${${pkg_name}_LIBRARIES}		CACHE INTERNAL "")	
	

	LIST(LENGTH "${pkg_name}_FOUND" list_length)
	IF(${list_length})
	SET(GLOBAL_PACKAGE_FOUND			${GLOBAL_PACKAGE_FOUND}			"${pkg_name}_FOUND" 		CACHE INTERNAL	"global list of all variables which identificate the found packages")
	ENDIF()
	
	LIST(REMOVE_ITEM "${pkg_name}_INCLUDE_DIRS" "")
	LIST(LENGTH "${pkg_name}_INCLUDE_DIRS" list_length)
	IF(${list_length})
	SET(GLOBAL_PACKAGE_INCLUDE_DIRS		${GLOBAL_PACKAGE_INCLUDE_DIRS} 	"${pkg_name}_INCLUDE_DIRS"	CACHE INTERNAL	"global list of all variables which store the include directories of the found packages")
	ENDIF()
	
	LIST(REMOVE_ITEM "${pkg_name}_LIBRARIES" "")
	LIST(LENGTH "${pkg_name}_LIBRARIES" list_length)
	IF(${list_length})
	SET(GLOBAL_PACKAGE_LIBRARIES		${GLOBAL_PACKAGE_LIBRARIES}		"${pkg_name}_LIBRARIES"		CACHE INTERNAL	"global list of all variables which store the libraries of the found packages")
	ENDIF()
ENDFUNCTION(INTERNAL_OPTIONAL_PACKAGE)


FUNCTION(INTERNAL_ADD_RELEASE_DEFINITION iard_definition)
	LIST(REMOVE_ITEM "iard_definition" "")
	LIST(LENGTH "iard_definition" list_length)
	IF(${list_length})
		SET(${CURRENT_MODULE_NAME}_RELEASE_DEFINITIONS ${${CURRENT_MODULE_NAME}_RELEASE_DEFINITIONS} ${iard_definition} CACHE INTERNAL "")
		SET(GLOBAL_MODULE_RELEASE_DEFINITIONS 		${GLOBAL_MODULE_RELEASE_DEFINITIONS} 		${CURRENT_MODULE_NAME}_RELEASE_DEFINITIONS 		CACHE INTERNAL "global list of all variables which store the release definitions for the specific module")
	ENDIF()
	
	LIST(LENGTH ${CURRENT_MODULE_NAME}_RELEASE_DEFINITIONS list_length)
	IF(list_length)
		LIST(REMOVE_DUPLICATES ${CURRENT_MODULE_NAME}_RELEASE_DEFINITIONS)
		SET(${CURRENT_MODULE_NAME}_RELEASE_DEFINITIONS ${${CURRENT_MODULE_NAME}_RELEASE_DEFINITIONS} CACHE INTERNAL "")
	ENDIF()
	
	LIST(LENGTH GLOBAL_MODULE_RELEASE_DEFINITIONS list_length)
	IF(list_length)
		LIST(REMOVE_DUPLICATES GLOBAL_MODULE_RELEASE_DEFINITIONS)
		SET(GLOBAL_MODULE_RELEASE_DEFINITIONS ${GLOBAL_MODULE_RELEASE_DEFINITIONS} CACHE INTERNAL "global list of all variables which store the release definitions for the specific module")
	ENDIF()
ENDFUNCTION(INTERNAL_ADD_RELEASE_DEFINITION)


FUNCTION(INTERNAL_ADD_DEBUG_DEFINITION iadd_definition)
	LIST(REMOVE_ITEM "iadd_definition" "")
	LIST(LENGTH "iadd_definition" list_length)
	IF(${list_length})
		SET(${CURRENT_MODULE_NAME}_DEBUG_DEFINITIONS ${${CURRENT_MODULE_NAME}_DEBUG_DEFINITIONS} ${iadd_definition} CACHE INTERNAL "")
		SET(GLOBAL_MODULE_DEBUG_DEFINITIONS 		${GLOBAL_MODULE_DEBUG_DEFINITIONS} 			${CURRENT_MODULE_NAME}_DEBUG_DEFINITIONS  		CACHE INTERNAL "global list of all variables which store the debug definitions for the specific module")
	ENDIF()

	LIST(LENGTH ${CURRENT_MODULE_NAME}_DEBUG_DEFINITIONS list_length)
	IF(list_length)
		LIST(REMOVE_DUPLICATES ${CURRENT_MODULE_NAME}_DEBUG_DEFINITIONS)
		SET(${CURRENT_MODULE_NAME}_DEBUG_DEFINITIONS ${${CURRENT_MODULE_NAME}_DEBUG_DEFINITIONS} CACHE INTERNAL "")
	ENDIF()
	
	LIST(LENGTH GLOBAL_MODULE_DEBUG_DEFINITIONS list_length)
	IF(list_length)
		LIST(REMOVE_DUPLICATES GLOBAL_MODULE_DEBUG_DEFINITIONS)
		SET(GLOBAL_MODULE_DEBUG_DEFINITIONS ${GLOBAL_MODULE_DEBUG_DEFINITIONS} CACHE INTERNAL "global list of all variables which store the debug definitions for the specific module")
	ENDIF()
ENDFUNCTION(INTERNAL_ADD_DEBUG_DEFINITION)


FUNCTION(INTERNAL_ADD_DEFINITION iad_definition)
	INTERNAL_ADD_RELEASE_DEFINITION("${iad_definition}")
	INTERNAL_ADD_DEBUG_DEFINITION("${iad_definition}")
ENDFUNCTION(INTERNAL_ADD_DEFINITION)


FUNCTION(INTERNAL_ADD_DEPENDENCY ad_name)
	MESSAGE(VERBOSE INTERNAL_ADD_DEPENDENCY "Adding dependency ${ad_name} to module ${CURRENT_MODULE_NAME}")
	IF(NOT "${ad_name}_FOUND")
		MESSAGE(VERBOSE INTERNAL_ADD_DEPENDENCY "Trying to satisfy dependency through find modules")
		INTERNAL_TRY_TO_SATISFY_DEPENDENCY_USING_FIND_PACKAGE(${ad_name})
	ENDIF()
	IF("${ad_name}_FOUND")
		MESSAGE(VERBOSE INTERNAL_ADD_DEPENDENCY "Dependency could be satisfied")
        SET(${CURRENT_MODULE_NAME}_DEPENDENCIES ${ad_name} ${${ad_name}_DEPENDENCIES} ${${CURRENT_MODULE_NAME}_DEPENDENCIES} CACHE INTERNAL "")
		LIST(REMOVE_DUPLICATES ${CURRENT_MODULE_NAME}_DEPENDENCIES)
		SET(${CURRENT_MODULE_NAME}_DEPENDENCIES ${${CURRENT_MODULE_NAME}_DEPENDENCIES} CACHE INTERNAL "")
		SET(GLOBAL_MODULE_DEPENDENCIES ${CURRENT_MODULE_NAME}_DEPENDENCIES ${GLOBAL_MODULE_DEPENDENCIES} CACHE INTERNAL "global list of all variables which store the dependencies of the specific module")
		
		FOREACH(iad_dependency ${${ad_name}_DEPENDENCIES})
			INTERNAL_ADD_DEFINITION("${${iad_dependency}_DEPENDENT_DEFINITIONS}")
			INTERNAL_ADD_DEBUG_DEFINITION("${${iad_dependency}_DEPENDENT_DEBUG_DEFINITIONS}")
			INTERNAL_ADD_DEBUG_DEFINITION("${${iad_dependency}_DEBUG_DEFINITIONS}")
			INTERNAL_ADD_RELEASE_DEFINITION("${${iad_dependency}_DEPENDENT_RELEASE_DEFINITIONS}")
			INTERNAL_ADD_RELEASE_DEFINITION("${${iad_dependency}_RELEASE_DEFINITIONS}")
			
			INTERNAL_LINK_LIBRARY(${iad_dependency})
		ENDFOREACH()
		
		CALL_PLUGIN_DEPENDENCY_HOOKS()

		INTERNAL_LINK_LIBRARY(${ad_name})
		
		INTERNAL_ADD_DEFINITION("${${ad_name}_DEPENDENT_DEFINITIONS}")
		INTERNAL_ADD_DEBUG_DEFINITION("${${ad_name}_DEPENDENT_DEBUG_DEFINITIONS}")
		INTERNAL_ADD_DEBUG_DEFINITION("${${ad_name}_DEBUG_DEFINITIONS}")
		INTERNAL_ADD_RELEASE_DEFINITION("${${ad_name}_DEPENDENT_RELEASE_DEFINITIONS}")
		INTERNAL_ADD_RELEASE_DEFINITION("${${ad_name}_RELEASE_DEFINITIONS}")
		
		
		
	ELSE()
		MESSAGE(WARNING "Required dependency '${ad_name}' was not found.")
		MESSAGE(WARNING "Build of '${CURRENT_MODULE_NAME}' was disabled.")
		MESSAGE(WARNING "Make dependency '${ad_name}' known to acme to enable build of '${CURRENT_MODULE_NAME}' (e.g. findmodule, plugin, external project..).")
		SET(${CURRENT_MODULE_NAME}_BUILD_ENABLED 0 CACHE INTERNAL "")
	ENDIF()
	#SET(${CURRENT_MODULE_NAME}_DEPENDENCIES ${${CURRENT_MODULE_NAME}_DEPENDENCIES} ${ad_name})
	
ENDFUNCTION(INTERNAL_ADD_DEPENDENCY)


FUNCTION(INTERNAL_ADD_DEBUG_COMPILER_FLAG aidcf_compiler_flag)
	LIST(REMOVE_ITEM "aidcf_compiler_flag" "")
	LIST(LENGTH "aidcf_compiler_flag" list_length)
	IF(${list_length})
		SET(${CURRENT_MODULE_NAME}_DEBUG_COMPILER_FLAGS ${${CURRENT_MODULE_NAME}_DEBUG_COMPILER_FLAGS} ${aidcf_compiler_flag} CACHE INTERNAL "")
		SET(GLOBAL_MODULE_DEBUG_COMPILER_FLAGS 	${GLOBAL_MODULE_DEBUG_COMPILER_FLAGS} 		${CURRENT_MODULE_NAME}_DEBUG_COMPILER_FLAGS		CACHE INTERNAL "global list of all variables which store the debug compiler flags for the specific module")	
	ENDIF()
	
	LIST(LENGTH ${CURRENT_MODULE_NAME}_DEBUG_COMPILER_FLAGS list_length)
	IF(${list_length})
		LIST(REMOVE_DUPLICATES ${CURRENT_MODULE_NAME}_DEBUG_COMPILER_FLAGS)
		SET(${CURRENT_MODULE_NAME}_DEBUG_COMPILER_FLAGS ${${CURRENT_MODULE_NAME}_DEBUG_COMPILER_FLAGS} CACHE INTERNAL "")
	ENDIF()
	
	LIST(LENGTH GLOBAL_MODULE_DEBUG_COMPILER_FLAGS list_length)
	IF(list_length)
		LIST(REMOVE_DUPLICATES GLOBAL_MODULE_DEBUG_COMPILER_FLAGS)
		SET(GLOBAL_MODULE_DEBUG_COMPILER_FLAGS ${GLOBAL_MODULE_DEBUG_COMPILER_FLAGS} CACHE INTERNAL "global list of all variables which store the debug compiler flags for the specific module")
	ENDIF()
ENDFUNCTION(INTERNAL_ADD_DEBUG_COMPILER_FLAG)


FUNCTION(INTERNAL_ADD_RELEASE_COMPILER_FLAG iarcf_compiler_flag)
	LIST(REMOVE_ITEM "iarcf_compiler_flag" "")
	LIST(LENGTH "iarcf_compiler_flag" list_length)
	IF(${list_length})
		SET(${CURRENT_MODULE_NAME}_RELEASE_COMPILER_FLAGS ${${CURRENT_MODULE_NAME}_RELEASE_COMPILER_FLAGS} ${iarcf_compiler_flag} CACHE INTERNAL "")
		SET(GLOBAL_MODULE_RELEASE_COMPILER_FLAGS 	${GLOBAL_MODULE_RELEASE_COMPILER_FLAGS} 	${CURRENT_MODULE_NAME}_RELEASE_COMPILER_FLAGS 	CACHE INTERNAL "global list of all variables which store the release compiler flags for the specific module")
	ENDIF()

	LIST(LENGTH ${CURRENT_MODULE_NAME}_RELEASE_COMPILER_FLAGS list_length)
	IF(${list_length})
		LIST(REMOVE_DUPLICATES ${CURRENT_MODULE_NAME}_RELEASE_COMPILER_FLAGS)
		SET(${CURRENT_MODULE_NAME}_RELEASE_COMPILER_FLAGS ${${CURRENT_MODULE_NAME}_RELEASE_COMPILER_FLAGS} CACHE INTERNAL "")
	ENDIF()
	
	LIST(LENGTH GLOBAL_MODULE_RELEASE_COMPILER_FLAGS list_length)
	IF(list_length)
		LIST(REMOVE_DUPLICATES GLOBAL_MODULE_RELEASE_COMPILER_FLAGS)
		SET(GLOBAL_MODULE_RELEASE_COMPILER_FLAGS ${GLOBAL_MODULE_RELEASE_COMPILER_FLAGS} CACHE INTERNAL "global list of all variables which store the release compiler flags for the specific module")
	ENDIF()
ENDFUNCTION(INTERNAL_ADD_RELEASE_COMPILER_FLAG)


FUNCTION(INTERNAL_ADD_COMPILER_FLAG aicf_compiler_flag)
	INTERNAL_ADD_DEBUG_COMPILER_FLAG(${aicf_compiler_flag})
	INTERNAL_ADD_RELEASE_COMPILER_FLAG(${aicf_compiler_flag})
ENDFUNCTION(INTERNAL_ADD_COMPILER_FLAG)


FUNCTION(INTERNAL_ADD_DEBUG_LINKER_FLAG aadl_linker_flag)
	LIST(REMOVE_ITEM "aadl_linker_flag" "")
	LIST(LENGTH "aadl_linker_flag" list_length)
	IF(${list_length})
		SET(${CURRENT_MODULE_NAME}_DEBUG_LINKER_FLAGS ${${CURRENT_MODULE_NAME}_DEBUG_LINKER_FLAGS} "${aadl_linker_flag}" CACHE INTERNAL "")
		SET(GLOBAL_MODULE_DEBUG_LINKER_FLAGS 		${GLOBAL_MODULE_DEBUG_LINKER_FLAGS} 		${CURRENT_MODULE_NAME}_DEBUG_LINKER_FLAGS 		CACHE INTERNAL "global list of all variables which store the debug linker flags for the specific module")
	ENDIF()

	LIST(LENGTH ${CURRENT_MODULE_NAME}_DEBUG_LINKER_FLAGS list_length)
	IF(${list_length})
		LIST(REMOVE_DUPLICATES ${CURRENT_MODULE_NAME}_DEBUG_LINKER_FLAGS)
		SET(${CURRENT_MODULE_NAME}_DEBUG_LINKER_FLAGS ${${CURRENT_MODULE_NAME}_DEBUG_LINKER_FLAGS} CACHE INTERNAL "")
	ENDIF()	

	LIST(LENGTH GLOBAL_MODULE_DEBUG_LINKER_FLAGS list_length)
	IF(list_length)
		LIST(REMOVE_DUPLICATES GLOBAL_MODULE_DEBUG_LINKER_FLAGS)
		SET(GLOBAL_MODULE_DEBUG_LINKER_FLAGS ${GLOBAL_MODULE_DEBUG_LINKER_FLAGS} CACHE INTERNAL "global list of all variables which store the debug linker flags for the specific module")
	ENDIF()	
ENDFUNCTION(INTERNAL_ADD_DEBUG_LINKER_FLAG)


FUNCTION(INTERNAL_ADD_RELEASE_LINKER_FLAG iarl_linker_flag)
	LIST(REMOVE_ITEM "iarl_linker_flag" "")
	LIST(LENGTH "iarl_linker_flag" list_length)
	IF(${list_length})
		SET(${CURRENT_MODULE_NAME}_RELEASE_LINKER_FLAGS ${${CURRENT_MODULE_NAME}_RELEASE_LINKER_FLAGS} "${iarl_linker_flag}" CACHE INTERNAL "")
		SET(GLOBAL_MODULE_RELEASE_LINKER_FLAGS 	${GLOBAL_MODULE_RELEASE_LINKER_FLAGS} 		${CURRENT_MODULE_NAME}_RELEASE_LINKER_FLAGS		CACHE INTERNAL "global list of all variables which store the release linker flags for the specific module")
	ENDIF()

	LIST(LENGTH ${CURRENT_MODULE_NAME}_RELEASE_LINKER_FLAGS list_length)
	IF(${list_length})
		LIST(REMOVE_DUPLICATES ${CURRENT_MODULE_NAME}_RELEASE_LINKER_FLAGS)
		SET(${CURRENT_MODULE_NAME}_RELEASE_LINKER_FLAGS ${${CURRENT_MODULE_NAME}_RELEASE_LINKER_FLAGS} CACHE INTERNAL "")
	ENDIF()	

	LIST(LENGTH GLOBAL_MODULE_RELEASE_LINKER_FLAGS list_length)
	IF(list_length)
		LIST(REMOVE_DUPLICATES GLOBAL_MODULE_RELEASE_LINKER_FLAGS)
		SET(GLOBAL_MODULE_RELEASE_LINKER_FLAGS ${GLOBAL_MODULE_RELEASE_LINKER_FLAGS} CACHE INTERNAL "global list of all variables which store the release linker flags for the specific module")
	ENDIF()		
ENDFUNCTION(INTERNAL_ADD_RELEASE_LINKER_FLAG)


FUNCTION(INTERNAL_ADD_LINKER_FLAG ialf_linker_flag)
	INTERNAL_ADD_DEBUG_LINKER_FLAG(${ialf_linker_flag})
	INTERNAL_ADD_RELEASE_LINKER_FLAG(${ialf_linker_flag})
	#SET(${CURRENT_MODULE_NAME}_LINKER_FLAGS ${${CURRENT_MODULE_NAME}_LINKER_FLAGS} ${ad_linker_flag} CACHE INTERNAL "")
ENDFUNCTION(INTERNAL_ADD_LINKER_FLAG)


FUNCTION(INTERNAL_ADD_FILE ac_name)
	INTERNAL_ARGUMENT_SPLITTER("${ARGN}" "SOURCE_VAR TEST_VAR SOURCE_GROUP PREFIX CONDITIONS" AC)
	
	IF(NOT "${CURRENT_MODULE_NAME}" STREQUAL "")
		STRING(TOUPPER ${CURRENT_MODULE_NAME} CURRENT_UPPER_MODULE_NAME)
	ENDIF()
	
	SET(ADD_FILE_CONDITIONS_MET 1)
	FOREACH(CONDITION ${AC_CONDITIONS})
		IF (NOT ${CONDITION})
			MESSAGE(VERBOSE  "Excluded class ${ac_name}, condition ${CONDITION} failed")
			SET(ADD_FILE_CONDITIONS_MET 0)
		ENDIF()
	ENDFOREACH()
	
	IF(${ADD_FILE_CONDITIONS_MET})
		SET(ac_all_classes ${ac_name} ${AC_DEFAULT_ARGS})	
		IF("${AC_PREFIX}" STREQUAL "")
			INTERNAL_SPLIT_PATH_AND_FILE(${ac_name} ac_path ac_filename)
			STRING(TOUPPER ${ac_filename} ac_varname)
			SET(AC_PREFIX ${ac_varname})
		ENDIF()

		INTERNAL_GET_PATH_TO_FILES(${ac_all_classes} PREFIX ${AC_PREFIX})
		IF(NOT "${${AC_PREFIX}_SOURCE_FILES}" STREQUAL "")
			SET(${CURRENT_MODULE_NAME}_HAS_SOURCE_FILES 1 CACHE INTERNAL "")
		ENDIF()

		IF(NOT "${AC_SOURCE_GROUP}" STREQUAL "")
			SET(${AC_SOURCE_GROUP} ${${AC_SOURCE_GROUP}} ${${AC_PREFIX}_INTERN_HEADER} ${${AC_PREFIX}_SOURCE_FILES} ${${AC_PREFIX}_PUBLIC_HEADER})
			INTERNAL_ADD_FILES_TO_SOURCE_GROUP("${AC_SOURCE_GROUP}" "${${AC_SOURCE_GROUP}}")
		ENDIF()
		
		
		IF(NOT "${AC_SOURCE_VAR}" STREQUAL "")
			SET(${AC_SOURCE_VAR} ${${AC_SOURCE_VAR}} ${${AC_PREFIX}_PUBLIC_HEADER} ${${AC_PREFIX}_INTERN_HEADER} ${${AC_PREFIX}_SOURCE_FILES})
		ELSE()
			SET(${CURRENT_UPPER_MODULE_NAME}_MODULE_SOURCE_FILES ${${CURRENT_UPPER_MODULE_NAME}_MODULE_SOURCE_FILES} ${${AC_PREFIX}_PUBLIC_HEADER} ${${AC_PREFIX}_INTERN_HEADER} ${${AC_PREFIX}_SOURCE_FILES} CACHE INTERNAL "")
			SET(GLOBAL_MODULE_SOURCE_FILES	${GLOBAL_MODULE_SOURCE_FILES}	${CURRENT_UPPER_MODULE_NAME}_MODULE_SOURCE_FILES CACHE INTERNAL "global list of all variables which store the source files of the specific module")
		ENDIF()
		
		IF(NOT "${AC_TEST_VAR}" STREQUAL "")
			SET(${AC_TEST_VAR} ${${AC_TEST_VAR}} ${${AC_PREFIX}_TEST_FILES})
		ELSE()

			LIST(LENGTH ${AC_PREFIX}_TEST_FILES list_length)
			IF(list_length)
				SET(${CURRENT_MODULE_NAME}_TEST_FILES ${${CURRENT_MODULE_NAME}_TEST_FILES} ${${AC_PREFIX}_TEST_FILES} CACHE INTERNAL "")
				SET(GLOBAL_MODULE_TEST_FILES	${GLOBAL_MODULE_TEST_FILES}		${CURRENT_MODULE_NAME}_TEST_FILES	CACHE INTERNAL "global list of all variables which store the test files of the specific module")
			ENDIF()
		ENDIF()
		
	ENDIF()
ENDFUNCTION(INTERNAL_ADD_FILE)


FUNCTION(INTERNAL_ADD_OPTIONAL_FILE aoc_name)
	INTERNAL_ARGUMENT_SPLITTER("${ARGN}" "SOURCE_VAR SOURCE_GROUP PREFIX CONDITIONS" AOC)
	IF("${AOC_PREFIX}" STREQUAL "")
		INTERNAL_SPLIT_PATH_AND_FILE(${aoc_name} aoc_path aoc_filename)
		STRING(TOUPPER ${aoc_filename} aoc_varname)
		SET(AOC_PREFIX ${aoc_varname})
	ENDIF()

	STRING(TOUPPER ${CURRENT_MODULE_NAME} CURRENT_UPPER_MODULE_NAME)
	SET(aoc_temp_files)
	SET(aoc_temp_test)

	INTERNAL_ADD_FILE(${aoc_name} ${AOC_DEFAULT_ARGS} SOURCE_VAR aoc_temp_files TEST_VAR aoc_temp_test SOURCE_GROUP ${AOC_SOURCE_GROUP} PREFIX ${AOC_PREFIX} CONDITIONS ${AOC_CONDITIONS})
	INTERNAL_ADD_OPTIONAL_FILES(${CURRENT_UPPER_MODULE_NAME}_${AOC_PREFIX} SOURCE_FILES ${aoc_temp_files} TEST_FILES ${aoc_temp_test})
	#SET(${CURRENT_UPPER_MODULE_NAME}_SOURCE_FILES ${${CURRENT_UPPER_MODULE_NAME}_MODULE_SOURCE_FILES} ${${CURRENT_UPPER_MODULE_NAME}_${AOC_PREFIX}_SOURCE_FILES})
	#SET(${CURRENT_MODULE_NAME}_TEST_FILES ${${CURRENT_MODULE_NAME}_TEST_FILES} ${${CURRENT_UPPER_MODULE_NAME}_${AOC_PREFIX}_TEST_FILES})
	
ENDFUNCTION(INTERNAL_ADD_OPTIONAL_FILE)


FUNCTION(INTERNAL_ADD_OPTIONAL_FILES aof_prefix)

	INTERNAL_ARGUMENT_SPLITTER("${ARGN}" "SOURCE_FILES TEST_FILES" AOF)
	STRING(TOUPPER ${CURRENT_MODULE_NAME} CURRENT_UPPER_MODULE_NAME)
	SET(${aof_prefix}_ENABLE "on" CACHE BOOL "Enable ${aof_prefix}" )
	SET(${aof_prefix}_SOURCE_FILES)
	SET(${aof_prefix}_TEST_FILES)
	SET(GLOBAL_MODULE_OPTIONAL_FILES 	${GLOBAL_MODULE_OPTIONAL_FILES}		${aof_prefix}_ENABLE 	CACHE 	INTERNAL "global list of variables which indicate if an optional file ist enabled or not")
	
	IF(${WITH_${CURRENT_UPPER_MODULE_NAME}})
		IF(${${aof_prefix}_ENABLE})
			SET(${aof_prefix}_SOURCE_FILES ${AOF_SOURCE_FILES})
			SET(${aof_prefix}_TEST_FILES   ${AOF_TEST_FILES})
		ENDIF()
	ELSE()
		INTERNAL_REMOVE_OPTIONAL_FILES(${aof_prefix})
	ENDIF()

	SET(${CURRENT_UPPER_MODULE_NAME}_MODULE_SOURCE_FILES 	${${CURRENT_UPPER_MODULE_NAME}_MODULE_SOURCE_FILES} 	${${aof_prefix}_SOURCE_FILES}	CACHE INTERNAL "")
	SET(${CURRENT_MODULE_NAME}_TEST_FILES 					${${CURRENT_MODULE_NAME}_TEST_FILES} 					${${aof_prefix}_TEST_FILES}		CACHE INTERNAL "")
	SET(GLOBAL_MODULE_SOURCE_FILES	${GLOBAL_MODULE_SOURCE_FILES}	${CURRENT_UPPER_MODULE_NAME}_MODULE_SOURCE_FILES 	CACHE INTERNAL "global list of all variables which store the source files of the specific module")
	SET(GLOBAL_MODULE_TEST_FILES	${GLOBAL_MODULE_TEST_FILES}		${CURRENT_MODULE_NAME}_TEST_FILES					CACHE INTERNAL "global list of all variables which store the test files of the specific module")

ENDFUNCTION(INTERNAL_ADD_OPTIONAL_FILES)


FUNCTION(INTERNAL_REMOVE_OPTIONAL_FILES prefix_unset)
	UNSET(${prefix_unset}_ENABLE CACHE)
ENDFUNCTION(INTERNAL_REMOVE_OPTIONAL_FILES)


FUNCTION(INTERNAL_LINK_LIBRARY ill_library)
	#LIST(FIND GLOBAL_LIBRARIES ${ill_library} FOUND_LIBRARY)
	#IF(${FOUND_LIBRARY} EQUAL -1)
	#	MESSAGE(FATAL_ERROR "You tried to link against the unknown lib ${ill_library}")
	#ENDIF()
	IF(${${ill_library}_HAS_SOURCE_FILES})
		SET(${CURRENT_MODULE_NAME}_LIBRARIES 	${${CURRENT_MODULE_NAME}_LIBRARIES} 		${ill_library} ${ARGN} 				CACHE INTERNAL "")
		SET(GLOBAL_MODULE_LIBRARIES 			${GLOBAL_MODULE_LIBRARIES} 					${CURRENT_MODULE_NAME}_LIBRARIES 	CACHE INTERNAL "global list of all variables which store the libraries of the specific module")	

		SET(${CURRENT_MODULE_NAME}_LIBRARY_DIRS	${${CURRENT_MODULE_NAME}_LIBRARY_DIRS} 		${ill_library} ${ARGN} 				CACHE INTERNAL "")
		SET(GLOBAL_MODULE_LIBRARY_DIRS 			${GLOBAL_MODULE_LIBRARY_DIRS} 					${CURRENT_MODULE_NAME}_LIBRARY_DIRS 	CACHE INTERNAL "global list of all variables which store the library dirs of the specific module")	
	ENDIF()
ENDFUNCTION(INTERNAL_LINK_LIBRARY)


FUNCTION(INTERNAL_LINK_LIBRARY_GROUP illg_library)
	SET(library_group ${illg_library} ${ARGN})
	INTERNAL_GROUP_LINK(library_group ${library_group})
	SET(${CURRENT_MODULE_NAME}_LIBRARIES 	${${CURRENT_MODULE_NAME}_LIBRARIES} 	${library_group}					CACHE INTERNAL "")
	SET(GLOBAL_MODULE_LIBRARIES 			${GLOBAL_MODULE_LIBRARIES} 				${CURRENT_MODULE_NAME}_LIBRARIES 	CACHE INTERNAL "global list of all variables which store the libraries of the specific module")
ENDFUNCTION(INTERNAL_LINK_LIBRARY_GROUP)


FUNCTION(INTERNAL_ADD_INSTALL_FILE iaif_filename)
	SET(${CURRENT_MODULE_NAME}_INSTALL_FILES 	${${CURRENT_MODULE_NAME}_INSTALL_FILES} 	"${iaif_filename}" 								CACHE INTERNAL "")
	SET(GLOBAL_MODULE_INSTALL_FILES 			${GLOBAL_MODULE_INSTALL_FILES} 				${CURRENT_MODULE_NAME}_INSTALL_FILES 			CACHE INTERNAL "global list of all variables which identificate the install files for the specific module")
ENDFUNCTION(INTERNAL_ADD_INSTALL_FILE)


FUNCTION(INTERNAL_INSTALL_HEADER_FILES ihf_header_file_path)
	INSTALL(DIRECTORY ${PROJECT_SOURCE_DIR}/modules/${CURRENT_MODULE_NAME}/include/${CURRENT_MODULE_NAME}/
	        DESTINATION ${ihf_header_file_path}
	        FILES_MATCHING
	        PATTERN ".*" EXCLUDE)
ENDFUNCTION(INTERNAL_INSTALL_HEADER_FILES)


FUNCTION(INTERNAL_INSTALL_RESOURCES)
	STRING(TOUPPER ${CURRENT_MODULE_NAME} CURRENT_UPPER_MODULE_NAME)
	IF(${WITH_${CURRENT_UPPER_MODULE_NAME}} AND ${${CURRENT_MODULE_NAME}_BUILD_ENABLED})
		INTERNAL_ARGUMENT_SPLITTER("${ARGN}" "FILES DESTINATION" RES)
		MESSAGE(VERBOSE "install resource files to ${RES_DESTINATION}")
		INSTALL(FILES ${RES_FILES} DESTINATION ${RES_DESTINATION})
	ENDIF()
ENDFUNCTION(INTERNAL_INSTALL_RESOURCES)


FUNCTION(INTERNAL_BUILD_UNIT_TESTS)
	IF(${CONFIG_BUILD_GLOBAL_TEST_EXECUTABLE} AND NOT "${GLOBAL_TEST_SOURCE}" STREQUAL "")
		#SET(CMAKE_RUNTIME_OUTPUT_DIRECTORY "${PROJECT_SOURCE_DIR}/deliverable/bin")
		#ADD_DEBUG_COMPILER_FLAG("/MTd")
		#ADD_RELEASE_COMPILER_FLAG("/MT")
		
		LIST(REMOVE_DUPLICATES GLOBAL_TEST_LINKER_DIRECTORIES)
		SET(GLOBAL_TEST_LINKER_DIRECTORIES  ${GLOBAL_TEST_LINKER_DIRECTORIES}  CACHE INTERNAL "collect test linker directories")
		LIST(REVERSE GLOBAL_TEST_LIBS)
        LIST(REMOVE_DUPLICATES GLOBAL_TEST_LIBS)
		LIST(REVERSE GLOBAL_TEST_LIBS)
		SET(GLOBAL_TEST_LIBS 	${GLOBAL_TEST_LIBS}        CACHE INTERNAL "collect test libs")
        LIST(REMOVE_DUPLICATES GLOBAL_TEST_INCLUDE_DIRECTORIES)
		SET(GLOBAL_TEST_INCLUDE_DIRECTORIES ${GLOBAL_TEST_INCLUDE_DIRECTORIES}	CACHE INTERNAL "collect test include directories")
		LIST(REMOVE_DUPLICATES GLOBAL_TEST_SOURCE)
		SET(GLOBAL_TEST_SOURCE  ${GLOBAL_TEST_SOURCE}	CACHE INTERNAL "collect test source")
		LIST(REMOVE_DUPLICATES GoogleMock_PACKAGE_LIBS)
		SET(GoogleMock_PACKAGE_LIBS  ${GoogleMock_PACKAGE_LIBS}	CACHE INTERNAL "")

		LINK_DIRECTORIES(${GoogleTest_LIBRARIES_DIR} ${GoogleMock_LIBRARIES_DIR} ${GLOBAL_TEST_LINKER_DIRECTORIES})
		INCLUDE_DIRECTORIES(SYSTEM ${GoogleTest_INCLUDE_DIRS} ${GoogleMock_INCLUDE_DIRS})
		INCLUDE_DIRECTORIES(${GLOBAL_TEST_INCLUDE_DIRECTORIES})
		MESSAGE(VERBOSE "Global unit test executable enabled, building Test")
		ADD_EXECUTABLE(Test ${GLOBAL_TEST_SOURCE})

		INTERNAL_ADD_COMPILER_FLAGS_TO_TARGET(Test ${GLOBAL_TEST_DEBUG_COMPILER_FLAGS} ${GLOBAL_TEST_RELEASE_COMPILER_FLAGS})
		#INTERNAL_ADD_RELEASE_COMPILER_FLAGS_TO_TARGET(Test ${GLOBAL_TEST_RELEASE_COMPILER_FLAGS})
		INTERNAL_ADD_DEBUG_LINKER_FLAGS_TO_TARGET(Test ${GLOBAL_TEST_DEBUG_LINKER_FLAGS})
		INTERNAL_ADD_RELEASE_LINKER_FLAGS_TO_TARGET(Test ${GLOBAL_TEST_RELEASE_LINKER_FLAGS})
		
		INTERNAL_ADD_DEBUG_DEFINITIONS_TO_TARGET(Test ${GLOBAL_TEST_DEBUG_DEFINITIONS})
		INTERNAL_ADD_RELEASE_DEFINITIONS_TO_TARGET(Test ${GLOBAL_TEST_RELEASE_DEFINITIONS})

		TARGET_LINK_LIBRARIES(Test gtest_main gmock_main ${GoogleMock_LIBRARIES} ${GoogleTest_LIBRARIES} ${GoogleMock_PACKAGE_LIBS} ${GoogleTest_PACKAGE_LIBS} ${GLOBAL_TEST_LIBS})
		SET_TARGET_PROPERTIES(Test PROPERTIES LINKER_LANGUAGE CXX)
		ADD_DEPENDENCIES(Test GoogleMock)
	ENDIF()
ENDFUNCTION(INTERNAL_BUILD_UNIT_TESTS)


FUNCTION(INTERNAL_REMOVE_DUPLICATES)
	LIST(REMOVE_DUPLICATES GLOBAL_EXTERN_INCLUDE_DIRS)		# removes duplicates in the list "GLOBAL_EXTERN_INCLUDE_DIRS"
	SET(GLOBAL_EXTERN_INCLUDE_DIRS     ${GLOBAL_EXTERN_INCLUDE_DIRS} 		CACHE INTERNAL "collect extern include dirs")
	
	LIST(REMOVE_DUPLICATES GLOBAL_MODULE_DEPENDENCIES)
	SET(GLOBAL_MODULE_DEPENDENCIES 	${GLOBAL_MODULE_DEPENDENCIES} CACHE INTERNAL "global list of all variables which store the dependencies of the specific module")
	
	LIST(REMOVE_DUPLICATES GLOBAL_MODULE_LIBRARIES)
	SET(GLOBAL_MODULE_LIBRARIES 	${GLOBAL_MODULE_LIBRARIES}	 CACHE INTERNAL "global list of all variables which store the libraries of the specific module")
	
	LIST(REMOVE_DUPLICATES GLOBAL_MODULE_DEBUG_COMPILER_FLAGS)
	SET(GLOBAL_MODULE_DEBUG_COMPILER_FLAGS 	${GLOBAL_MODULE_DEBUG_COMPILER_FLAGS} CACHE INTERNAL "global list of all variables which store the debug compiler flags for the specific module")
	
	LIST(REMOVE_DUPLICATES GLOBAL_MODULE_RELEASE_COMPILER_FLAGS)
	SET(GLOBAL_MODULE_RELEASE_COMPILER_FLAGS ${GLOBAL_MODULE_RELEASE_COMPILER_FLAGS} CACHE INTERNAL "global list of all variables which store the release compiler flags for the specific module")
	
	LIST(REMOVE_DUPLICATES GLOBAL_MODULE_DEBUG_LINKER_FLAGS)
	SET(GLOBAL_MODULE_DEBUG_LINKER_FLAGS 	${GLOBAL_MODULE_DEBUG_LINKER_FLAGS} CACHE INTERNAL "global list of all variables which store the debug linker flags for the specific module")

	LIST(REMOVE_DUPLICATES GLOBAL_MODULE_RELEASE_LINKER_FLAGS)
	SET(GLOBAL_MODULE_RELEASE_LINKER_FLAGS 	${GLOBAL_MODULE_RELEASE_LINKER_FLAGS} CACHE INTERNAL "global list of all variables which store the release linker flags for the specific module")

	LIST(REMOVE_DUPLICATES GLOBAL_MODULE_DEBUG_DEFINITIONS)
	SET(GLOBAL_MODULE_DEBUG_DEFINITIONS ${GLOBAL_MODULE_DEBUG_DEFINITIONS} CACHE INTERNAL "global list of all variables which store the debug definitions for the specific module")

	LIST(REMOVE_DUPLICATES GLOBAL_MODULE_RELEASE_DEFINITIONS)
	SET(GLOBAL_MODULE_RELEASE_DEFINITIONS ${GLOBAL_MODULE_RELEASE_DEFINITIONS} CACHE INTERNAL "global list of all variables which store the release definitions for the specific module")
	
	LIST(REMOVE_DUPLICATES GLOBAL_MODULE_INSTALL_FILES)
	SET(GLOBAL_MODULE_INSTALL_FILES ${GLOBAL_MODULE_INSTALL_FILES} CACHE INTERNAL "global list of all variables which identificate the install files for the specific module")

	LIST(REMOVE_DUPLICATES GLOBAL_MODULE_PACKAGE_LIBS)
	SET(GLOBAL_MODULE_PACKAGE_LIBS 	${GLOBAL_MODULE_PACKAGE_LIBS} CACHE INTERNAL "global list of all variables which identificate the package libaries for the specific module")
	
	LIST(REMOVE_DUPLICATES GLOBAL_MODULE_TEST_FILES)
	SET(GLOBAL_MODULE_TEST_FILES ${GLOBAL_MODULE_TEST_FILES} CACHE INTERNAL "global list of all variables which store the test files of the specific module")
	
	LIST(REMOVE_DUPLICATES GLOBAL_MODULE_SOURCE_FILES)
	SET(GLOBAL_MODULE_SOURCE_FILES	${GLOBAL_MODULE_SOURCE_FILES} CACHE INTERNAL "global list of all variables which store the source files of the specific module")

	LIST(REMOVE_DUPLICATES GLOBAL_MODULE_OPTIONAL_FILES)
	SET(GLOBAL_MODULE_OPTIONAL_FILES ${GLOBAL_MODULE_OPTIONAL_FILES} CACHE INTERNAL "global list of variables which indicate if an optional file ist enabled or not")
	
	LIST(REMOVE_DUPLICATES GLOBAL_EXTERNAL_LIBRARY_DEPENDENT_DEBUG_DEFINITIONS)
	SET(GLOBAL_EXTERNAL_LIBRARY_DEPENDENT_DEBUG_DEFINITIONS		${GLOBAL_EXTERNAL_LIBRARY_DEPENDENT_DEBUG_DEFINITIONS} CACHE INTERNAL "global list of all variables which indicate the dependent debug definitions of external libraries")
	
	LIST(REMOVE_DUPLICATES GLOBAL_EXTERNAL_LIBRARY_DEPENDENT_RELEASE_DEFINITIONS)
	SET(GLOBAL_EXTERNAL_LIBRARY_DEPENDENT_RELEASE_DEFINITIONS	${GLOBAL_EXTERNAL_LIBRARY_DEPENDENT_RELEASE_DEFINITIONS} CACHE INTERNAL "global list of all variables which indicate the dependent release definitions of external libraries")

	LIST(REVERSE GLOBAL_TEST_LIBS)
	LIST(REMOVE_DUPLICATES GLOBAL_TEST_LIBS)
	LIST(REVERSE GLOBAL_TEST_LIBS)
	SET(GLOBAL_TEST_LIBS 	${GLOBAL_TEST_LIBS}        CACHE INTERNAL "collect test libs")
	
	LIST(REMOVE_DUPLICATES GLOBAL_TEST_SOURCE)
	SET(GLOBAL_TEST_SOURCE  ${GLOBAL_TEST_SOURCE}	CACHE INTERNAL "collect test source")
	
	LIST(REMOVE_DUPLICATES GLOBAL_TEST_INCLUDE_DIRECTORIES)
	SET(GLOBAL_TEST_INCLUDE_DIRECTORIES ${GLOBAL_TEST_INCLUDE_DIRECTORIES}	CACHE INTERNAL "collect test include directories")
	
	LIST(REMOVE_DUPLICATES GLOBAL_TEST_LINKER_DIRECTORIES)
	SET(GLOBAL_TEST_LINKER_DIRECTORIES  ${GLOBAL_TEST_LINKER_DIRECTORIES}  CACHE INTERNAL "collect test linker directories")
	
	LIST(REMOVE_DUPLICATES GLOBAL_PACKAGE_FOUND)
	SET(GLOBAL_PACKAGE_FOUND	${GLOBAL_PACKAGE_FOUND}	CACHE INTERNAL	"global list of all variables which identificate the found packages")
	
	LIST(REMOVE_DUPLICATES GLOBAL_CMAKE_PROJECTS)
	SET(GLOBAL_CMAKE_PROJECTS	${GLOBAL_CMAKE_PROJECTS}	CACHE INTERNAL	"global list of all added cmake project")
ENDFUNCTION(INTERNAL_REMOVE_DUPLICATES)


FUNCTION(INTERNAL_REPORT)
	IF(NOT "${GLOBAL_UTILS_MODULES_STATIC}" STREQUAL "")
		INTERNAL_REPORT_MODULE(TYPE static MODULES ${GLOBAL_UTILS_MODULES_STATIC})
		MESSAGE(STATUS "-------------------------------------------------------------------")
	ENDIF()
	
	IF(NOT "${GLOBAL_UTILS_MODULES_DYNAMIC}" STREQUAL "")
		INTERNAL_REPORT_MODULE(TYPE dynamic MODULES ${GLOBAL_UTILS_MODULES_DYNAMIC})
		MESSAGE(STATUS "-------------------------------------------------------------------")
	ENDIF()
	
	IF(NOT "${GLOBAL_UTILS_MODULES_EXE}" STREQUAL "")
		INTERNAL_REPORT_MODULE(TYPE executables MODULES ${GLOBAL_UTILS_MODULES_EXE})
		MESSAGE(STATUS "-------------------------------------------------------------------")
	ENDIF()
	
	IF(NOT "${GLOBAL_UTILS_MODULES_TESTS}" STREQUAL "")
		INTERNAL_REPORT_MODULE(TYPE tests MODULES ${GLOBAL_UTILS_MODULES_TESTS})
		MESSAGE(STATUS "-------------------------------------------------------------------")
	ENDIF()
ENDFUNCTION(INTERNAL_REPORT)


FUNCTION(INTERNAL_REPORT_MODULE)
	INTERNAL_ARGUMENT_SPLITTER("${ARGN}" "TYPE MODULES" RM)
	MESSAGE(STATUS)
	MESSAGE(STATUS "Building ${RM_TYPE} modules:")
	MESSAGE(STATUS)
	FOREACH(r_module ${RM_MODULES})
		MESSAGE(STATUS "${r_module}")
	ENDFOREACH()
	MESSAGE(STATUS)
ENDFUNCTION(INTERNAL_REPORT_MODULE)


FUNCTION(INTERNAL_FINALIZE)
	INTERNAL_REMOVE_DUPLICATES()
	INTERNAL_BUILD_UNIT_TESTS()
	INTERNAL_REPORT()
	CALL_PLUGIN_FINALIZE_HOOKS()
ENDFUNCTION(INTERNAL_FINALIZE)


#MACRO(INTERNAL_ADD_SOURCE_FILES destination)
#	INTERNAL_ARGUMENT_SPLITTER("${ARGN}" "PUBLIC_HEADER INTERN_HEADER SOURCE_FILES" ARG)	
#	SET(${destination} ${${destination}} ${ARG_PUBLIC_HEADER} ${ARG_INTERN_HEADER} ${ARG_SOURCE_FILES})
#ENDMACRO(INTERNAL_ADD_SOURCE_FILES)


#MACRO(INTERNAL_INSTALL_MODULE)
#	STRING(TOUPPER ${CURRENT_MODULE_NAME} CURRENT_UPPER_MODULE_NAME)
#	IF(${WITH_${CURRENT_UPPER_MODULE_NAME}} AND ${${CURRENT_MODULE_NAME}_BUILD_ENABLED})
#	
#		INTERNAL_ARGUMENT_SPLITTER("${ARGN}" "HEADERS EXECUTABLE LIBRARY" INSTALL_PATH)
#	
#		IF(${INSTALL_PATH_HEADERS_FOUND})
#			MESSAGE(STATUS "Install header files to ${INSTALL_PATH_HEADERS}")
#			INTERNAL_INSTALL_HEADER_FILES(${INSTALL_PATH_HEADERS})
#		ENDIF(${INSTALL_PATH_HEADERS_FOUND})
#	
#		IF(${INSTALL_PATH_EXECUTABLE_FOUND})
#			MESSAGE(STATUS "Install executable files to ${INSTALL_PATH_EXECUTABLE}")
#			SET(INSTALL_COMMAND RUNTIME DESTINATION ${INSTALL_PATH_EXECUTABLE})
#		ENDIF(${INSTALL_PATH_EXECUTABLE_FOUND})
#	
#		IF(${INSTALL_PATH_LIBRARY_FOUND})
#			MESSAGE(STATUS "Install library files to ${INSTALL_PATH_LIBRARY}")
#			SET(INSTALL_COMMAND ${INSTALL_COMMAND} LIBRARY DESTINATION ${INSTALL_PATH_LIBRARY})
#			SET(INSTALL_COMMAND ${INSTALL_COMMAND} ARCHIVE DESTINATION ${INSTALL_PATH_LIBRARY})
#		ENDIF(${INSTALL_PATH_LIBRARY_FOUND})
#	
#		IF(NOT "${INSTALL_COMMAND}" STREQUAL "")
#			INSTALL(TARGETS ${CURRENT_MODULE_NAME} LIBRARY DESTINATION ${INSTALL_COMMAND})
#		ENDIF(NOT "${INSTALL_COMMAND}" STREQUAL "")
#		
#	ENDIF()
#	
#ENDMACRO(INTERNAL_INSTALL_MODULE)

