package dev.henkle.markdown.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.henkle.markdown.ui.model.UIElement
import dev.henkle.markdown.ui.utils.LocalMarkdownStyle

@Composable
fun MarkdownCodeBlock(element: UIElement.CodeBlock) {
    val style = LocalMarkdownStyle.current.codeBlock
    // TODO(garrison)
    Column {
        Text(
            modifier = Modifier
                .background(color = style.backgroundColor)
                .padding(
                    horizontal = style.horizontalPadding,
                    vertical = style.verticalPadding,
                ),
            text = element.code,
            style = style.textStyle,
        )
    }
}
