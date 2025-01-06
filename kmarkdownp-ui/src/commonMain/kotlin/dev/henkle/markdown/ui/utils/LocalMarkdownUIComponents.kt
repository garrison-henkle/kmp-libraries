package dev.henkle.markdown.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import dev.henkle.markdown.ui.MarkdownUIComponents

val LocalMarkdownUIComponents = compositionLocalOf<MarkdownUIComponents> {
    error("No MarkdownUIComponents provided to ProvideMarkdownUIComponents!")
}

@Composable
fun ProvideMarkdownUIComponents(components: MarkdownUIComponents, content: @Composable () -> Unit) {
    CompositionLocalProvider(
        value = LocalMarkdownUIComponents provides components,
        content = content,
    )
}
