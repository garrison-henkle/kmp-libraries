package dev.henkle.markdown.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import dev.henkle.markdown.ui.generator.Label
import dev.henkle.markdown.ui.generator.Url

val LocalMarkdownUrls = compositionLocalOf<Map<Label, Url>> {
    error("No urls provided to ProvideMarkdownUrls!")
}

@Composable
fun ProvideMarkdownUrls(urls: Map<Label, Url>, content: @Composable () -> Unit) {
    CompositionLocalProvider(
        value = LocalMarkdownUrls provides urls,
        content = content,
    )
}
