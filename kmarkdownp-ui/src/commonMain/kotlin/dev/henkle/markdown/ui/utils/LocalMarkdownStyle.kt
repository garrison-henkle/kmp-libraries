package dev.henkle.markdown.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import dev.henkle.markdown.ui.MarkdownStyle

val LocalMarkdownStyle = compositionLocalOf<MarkdownStyle> {
    error("No MarkdownStyle was provided to ProvideLocalMarkdownStyle!")
}

@Composable
fun ProvideMarkdownStyle(style: MarkdownStyle, content: @Composable () -> Unit) {
    CompositionLocalProvider(
        value = LocalMarkdownStyle provides style,
        content = content,
    )
}
