// Copyright 2015 Square, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// this buildscript is a modified version of the Cashapp Zipline buildscript here:
// https://github.com/cashapp/zipline/blob/f980d186afb720220a443704b6f2fabeba6c7229/zipline/build.zig

const std = @import("std");

pub fn build(b: *std.Build) !void {
  // The Windows builds create a .lib file in the lib/ directory which we don't need.
  const deleteLib = b.addRemoveDirTree(b.getInstallPath(.prefix, "lib"));
  b.getInstallStep().dependOn(&deleteLib.step);

  try setupTarget(b, &deleteLib.step, .linux, .aarch64, "linux_arm64");
  try setupTarget(b, &deleteLib.step, .linux, .x86_64, "linux_amd64");
  try setupTarget(b, &deleteLib.step, .macos, .aarch64, "mac_arm64");
  try setupTarget(b, &deleteLib.step, .macos, .x86_64, "mac_amd64");
}

fn setupTarget(b: *std.Build, step: *std.Build.Step, tag: std.Target.Os.Tag, arch: std.Target.Cpu.Arch, dir: []const u8) !void {
  const lib = b.addSharedLibrary(.{
    .name = "mdgrammars",
    .target = b.resolveTargetQuery(.{
      .cpu_arch = arch,
      .os_tag = tag,
      // We need to explicitly specify gnu for linux, as otherwise it defaults to musl.
      // See https://github.com/ziglang/zig/issues/16624#issuecomment-1801175600.
      .abi = if (tag == .linux) .gnu else null,
    }),
    .optimize = .ReleaseSmall,
  });

  lib.addIncludePath(b.path("src/native/include"));
  lib.addIncludePath(
    switch (tag) {
      .windows => b.path("src/native/include/windows"),
      else => b.path("src/native/include/posix"),
    }
  );

  lib.linkLibC();
  lib.addCSourceFiles(.{
    .files = &.{
      "src/native/markdown/parser.c",
      "src/native/markdown/scanner.c",
      "src/native/markdown-inline/parser.c",
      "src/native/markdown-inline/scanner.c",
      "src/native/jni_bindings.c",
    },
    .flags = &.{
      "-std=gnu99",
    },
  });

  const install = b.addInstallArtifact(lib, .{
    .dest_dir = .{
      .override = .{
        .custom = dir,
      },
    },
  });

  step.dependOn(&install.step);
}
