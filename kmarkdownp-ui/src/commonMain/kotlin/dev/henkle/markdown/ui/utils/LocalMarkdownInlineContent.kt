package dev.henkle.markdown.ui.utils

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf

val LocalMarkdownInlineContent = compositionLocalOf<Map<String, InlineTextContent>> {
    error("No InlineTextContent provided to ProvideMarkdownInlineContent!")
}

@Composable
fun ProvideMarkdownInlineContent(
    inlineContent: Map<String, InlineTextContent>,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        value = LocalMarkdownInlineContent provides inlineContent,
        content = content,
    )
}
