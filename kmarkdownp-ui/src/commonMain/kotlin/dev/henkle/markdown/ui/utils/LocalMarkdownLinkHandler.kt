package dev.henkle.markdown.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf

val LocalMarkdownLinkHandler = compositionLocalOf<(label: String, url: String) -> Unit> {
    error("No link handler was passed to ProvideMarkdownLinkHandler!")
}

@Composable
fun ProvideMarkdownLinkHandler(handler: (label: String, url: String) -> Unit, content: @Composable () -> Unit) {
    CompositionLocalProvider(
        value = LocalMarkdownLinkHandler provides handler,
        content = content,
    )
}
