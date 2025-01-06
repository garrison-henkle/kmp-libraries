const std = @import("std");

pub fn build(b: *std.Build) !void {
  // The Windows builds create a .lib file in the lib/ directory which we don't need.
  const deleteLib = b.addRemoveDirTree(b.getInstallPath(.prefix, "lib"));
  b.getInstallStep().dependOn(&deleteLib.step);

  try setupTarget(b, &deleteLib.step, .linux, .aarch64, "arm64");
  try setupTarget(b, &deleteLib.step, .linux, .x86_64, "amd64");
  try setupTarget(b, &deleteLib.step, .macos, .aarch64, "aarch64");
  try setupTarget(b, &deleteLib.step, .macos, .x86_64, "x86_64");
}

fn setupTarget(b: *std.Build, step: *std.Build.Step, tag: std.Target.Os.Tag, arch: std.Target.Cpu.Arch, dir: []const u8) !void {
  const lib = b.addSharedLibrary(.{
    .name = "markdown-grammar",
    .target = b.resolveTargetQuery(.{
      .cpu_arch = arch,
      .os_tag = tag,
      // We need to explicitly specify gnu for linux, as otherwise it defaults to musl.
      // See https://github.com/ziglang/zig/issues/16624#issuecomment-1801175600.
      .abi = if (tag == .linux) .gnu else null,
    }),
    .optimize = .ReleaseSmall,
  });

  lib.addIncludePath(b.path("src/jni/include"));
  lib.addIncludePath(
    switch (tag) {
      .windows => b.path("src/jni/include/windows"),
      else => b.path("src/jni/include/posix"),
    }
  );

  lib.linkLibC();
  lib.addCSourceFiles(.{
    .files = &.{
      "src/jni/markdown/parser.c",
      "src/jni/markdown/scanner.c",
      "src/jni/markdown-inline/parser.c",
      "src/jni/markdown-inline/scanner.c",
      "src/jni/jni_binding.c",
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
