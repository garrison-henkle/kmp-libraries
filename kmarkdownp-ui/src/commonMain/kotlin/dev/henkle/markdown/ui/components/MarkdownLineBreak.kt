package dev.henkle.markdown.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.henkle.markdown.ui.model.UIElement
import dev.henkle.markdown.ui.utils.LocalMarkdownStyle

@Composable
fun MarkdownLineBreak(
    modifier: Modifier = Modifier,
    element: UIElement.LineBreak,
){
    val newlineHeight = LocalMarkdownStyle.current.lineBreak.newlineHeight
    val count = remember(element) {
        (element.newlineCount - 1).coerceAtLeast(minimumValue = 0)
    }
    Spacer(modifier = modifier.height(height = newlineHeight * count))
}
