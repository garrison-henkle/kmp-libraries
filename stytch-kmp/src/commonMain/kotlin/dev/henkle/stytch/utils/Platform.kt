package dev.henkle.stytch.utils

@Suppress("EnumEntryName", "MemberVisibilityCanBePrivate")
enum class Platform {
    Android,
    iOS,
    macOS,
    macOSJvm,
    WindowsJvm,
    LinuxJvm,
    UnknownJvm,
    JSWasm,
    JS,
    ;

    val isAndroid: Boolean get() = this == Android
    val isNativeDarwin: Boolean get() = this == macOS || this == iOS
    val isNativeMacOS: Boolean get() = this == macOS
    val isIOS: Boolean get() = this == iOS
    val isDesktopJVM: Boolean get() =
        this == macOSJvm || this == WindowsJvm || this == LinuxJvm || this == UnknownJvm
    val isJVM: Boolean get() = isDesktopJVM || this == Android
    val isJS: Boolean get() = this == JS
    val isJSWasm: Boolean get() = this == JSWasm
    val isWeb: Boolean get() = isJS || isJSWasm
}
