package dev.henkle.compose.ktab.demo

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() {
    application {
        Window(title = "KTab Demo", onCloseRequest = ::exitApplication) {
            App()
        }
    }
}
