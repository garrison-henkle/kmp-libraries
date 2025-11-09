package dev.henkle.compose.ktab.demo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.henkle.compose.ktab.ui.rememberTabManager

private const val DEFAULT_TITLE = "KTab Demo"

fun main() {
    application {
        val manager = rememberTabManager<Tab>()
        val title by produceState(initialValue = DEFAULT_TITLE) {
            snapshotFlow { manager.lastFocusedTab }
                .collect { tab ->
                    value = tab?.title?.let { title -> "$DEFAULT_TITLE - $title" }
                        ?: DEFAULT_TITLE
                }
        }
        Window(title = title, onCloseRequest = ::exitApplication) {
            App(manager = manager)
        }
    }
}
