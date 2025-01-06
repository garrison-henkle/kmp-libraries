package dev.henkle.markdown.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.henkle.markdown.ui.model.UIElement
import dev.henkle.markdown.ui.utils.LocalMarkdownStyle

@Suppress("UNUSED_PARAMETER")
@Composable
fun MarkdownDivider(
    modifier: Modifier = Modifier,
    element: UIElement.Divider,
) {
    val style = LocalMarkdownStyle.current.divider
    Divider(
        modifier = modifier.fillMaxWidth(),
        color = style.color,
        thickness = style.thickness,
    )
}
