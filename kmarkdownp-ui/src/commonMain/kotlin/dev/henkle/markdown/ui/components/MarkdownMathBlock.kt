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
fun MarkdownMathBlock(element: UIElement.MathBlock) {
    val style = LocalMarkdownStyle.current.mathBlock
    // TODO(garrison):
    Column {
        Text(
            modifier = Modifier
                .background(color = style.backgroundColor)
                .padding(
                    horizontal = style.horizontalPadding,
                    vertical = style.verticalPadding,
                ),
            text = element.equation,
            style = style.textStyle,
        )
    }
}
