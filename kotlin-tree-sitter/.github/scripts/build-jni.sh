#!/bin/bash -eu

cmake -S kotlin-tree-sitter/ktreesitter -B kotlin-tree-sitter/ktreesitter/.cmake/build \
      -DCMAKE_BUILD_TYPE=RelWithDebugInfo \
      -DCMAKE_VERBOSE_MAKEFILE=ON \
      -DCMAKE_OSX_ARCHITECTURES=arm64 \
      -DCMAKE_INSTALL_PREFIX=kotlin-tree-sitter/ktreesitter/src/jvmMain/resources \
      -DCMAKE_INSTALL_LIBDIR="$CMAKE_INSTALL_LIBDIR"
cmake --build kotlin-tree-sitter/ktreesitter/.cmake/build
cmake --install kotlin-tree-sitter/ktreesitter/.cmake/build

for dir in kotlin-tree-sitter/languages/*/; do
    cmake -S "${dir}build/generated" -B "${dir}.cmake/build" \
          -DCMAKE_BUILD_TYPE=RelWithDebugInfo \
          -DCMAKE_OSX_ARCHITECTURES=arm64 \
          -DCMAKE_INSTALL_PREFIX="${dir}build/generated/src/jvmMain/resources" \
          -DCMAKE_INSTALL_LIBDIR="$CMAKE_INSTALL_LIBDIR"
    cmake --build "${dir}.cmake/build"
    cmake --install "${dir}.cmake/build"
done
