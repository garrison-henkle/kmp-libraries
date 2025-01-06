package dev.henkle.stytch.demo

import androidx.compose.ui.window.Window
import platform.AppKit.NSApp
import platform.AppKit.NSApplication

fun main() {
    NSApplication.sharedApplication()
    Window("Chat App") {
        App()
    }
    NSApp?.run()
}
