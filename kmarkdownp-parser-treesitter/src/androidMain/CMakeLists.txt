# Automatically generated file. DO NOT MODIFY

cmake_minimum_required(VERSION 3.4.1)

project(tree-sitter-markdown LANGUAGES C)

set(JAVA_AWT_LIBRARY NotNeeded)
set(JAVA_JVM_LIBRARY NotNeeded)
set(JAVA_INCLUDE_PATH2 NotNeeded)
set(JAVA_AWT_INCLUDE_PATH NotNeeded)
find_package(JNI REQUIRED)

set(CMAKE_C_STANDARD 11)

if(MSVC)
    add_compile_options(/W3 /wd4244)
else(MSVC)
    set(CMAKE_C_VISIBILITY_PRESET hidden)
    add_compile_options(-Wall -Wextra
                        -Wno-unused-parameter
                        -Werror=implicit-function-declaration)
endif(MSVC)

include_directories(${JNI_INCLUDE_DIRS} src/native/include src/native/include/posix src/native/markdown src/native/markdown-inline)

add_compile_definitions(TREE_SITTER_HIDE_SYMBOLS)

add_library(
    # library name
    mdgrammars

    # generate shared library
    SHARED

    # source files relative to the CMakeList.txt
    ../native/jni_bindings.c
    ../native/markdown/parser.c
    ../native/markdown/scanner.c
    ../native/markdown-inline/parser.c
    ../native/markdown-inline/scanner.c
)

set_target_properties(mdgrammars PROPERTIES DEFINE_SYMBOL "")

install(TARGETS mdgrammars ARCHIVE EXCLUDE_FROM_ALL)
