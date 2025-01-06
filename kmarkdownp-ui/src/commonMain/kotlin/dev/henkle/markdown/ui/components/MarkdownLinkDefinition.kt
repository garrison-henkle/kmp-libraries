package dev.henkle.markdown.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.henkle.markdown.ui.MarkdownContent
import dev.henkle.markdown.ui.components.shared.MarkdownLink
import dev.henkle.markdown.ui.model.UIElement
import dev.henkle.markdown.ui.utils.LocalMarkdownStyle
import dev.henkle.markdown.ui.utils.ProvideMarkdownStyle

@Composable
fun MarkdownLinkDefinition(modifier: Modifier = Modifier, element: UIElement.LinkDefinition) {
    val markdownStyle = LocalMarkdownStyle.current
    val style = markdownStyle.linkDefinition
    Row (
        modifier = modifier,
        verticalAlignment = style.rowAlignment,
        horizontalArrangement = Arrangement.spacedBy(space = style.spacing),
    ) {
        MarkdownLink(
            title = element.title,
            label = element.label,
            labelRaw = element.labelRaw,
            style = style.linkStyle,
        )
        element.title?.also { titleElements ->
            ProvideMarkdownStyle(style = markdownStyle.copy(text = style.contentStyle)) {
                MarkdownContent(elements = titleElements)
            }
        } ?: run {
            Text(
                text = element.url,
                style = style.contentStyle,
            )
        }
    }
}
