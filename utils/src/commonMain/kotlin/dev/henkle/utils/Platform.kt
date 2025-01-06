package dev.henkle.utils

@Suppress("EnumEntryName")
enum class Platform {
    Android,
    iOS,
    Js,
    Jvm,
    Linux,
    MacOS,
    Wasm,
    ;

    val isAndroid: Boolean get() = this == Android
    val isIOS: Boolean get() = this == iOS
    val isJs: Boolean get() = this == Js
    val isJvm: Boolean get() = this == Jvm
    val isLinux: Boolean get() = this == Linux
    val isMacOS: Boolean get() = this == MacOS
    val isWasm: Boolean get() = this == Wasm

    val isJvmBased: Boolean get() = this == Android || this == Jvm
    val isWeb: Boolean get() = this == Js || this == Wasm
    val isNative: Boolean get() = this == iOS || this == Linux || this == MacOS
    val isApple: Boolean get() = this == iOS || this == MacOS
}
