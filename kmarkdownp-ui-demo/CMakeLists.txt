# Automatically generated file. DO NOT MODIFY

cmake_minimum_required(VERSION 3.12.0)

project(ktreesitter-java LANGUAGES C)

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

include_directories(${JNI_INCLUDE_DIRS} ../../../../tree-sitter-markdown/tree-sitter-markdown/bindings/c)

add_compile_definitions(TREE_SITTER_HIDE_SYMBOLS)

add_library(ktreesitter-markdown SHARED src/jni/binding.c ../../../../tree-sitter-markdown/tree-sitter-markdown/src/parser.c ../../../../tree-sitter-markdown/tree-sitter-markdown/src/scanner.c)

set_target_properties(ktreesitter-markdown PROPERTIES DEFINE_SYMBOL "")

install(TARGETS ktreesitter-markdown ARCHIVE EXCLUDE_FROM_ALL)
