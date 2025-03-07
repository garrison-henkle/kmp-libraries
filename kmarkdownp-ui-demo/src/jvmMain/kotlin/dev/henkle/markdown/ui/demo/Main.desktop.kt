package dev.henkle.markdown.ui.demo

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() {
    application {
        Window(onCloseRequest = ::exitApplication, title = "KMarkdownP UI Demo") {
            App()
        }
    }
}
