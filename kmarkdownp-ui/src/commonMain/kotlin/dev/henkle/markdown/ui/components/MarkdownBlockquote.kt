package dev.henkle.markdown.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import dev.henkle.markdown.ui.MarkdownContent
import dev.henkle.markdown.ui.model.UIElement
import dev.henkle.markdown.ui.utils.LocalMarkdownStyle
import dev.henkle.markdown.ui.utils.ProvideMarkdownStyle

@Composable
fun MarkdownBlockquote(
    modifier: Modifier = Modifier,
    element: UIElement.Blockquote,
) {
    val markdownStyle = LocalMarkdownStyle.current
    val style = markdownStyle.blockquote
    Row(
        modifier = modifier
            .padding(start = style.quoteBarStartMargin)
            .height(intrinsicSize = IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(width = style.quoteBarWidth)
                .clip(shape = style.quoteBarShape)
                .background(color = style.quoteBarColor),
        )
        Spacer(modifier = Modifier.width(width = style.quoteBarEndMargin))
        ProvideMarkdownStyle(style = markdownStyle.copy(text = style.textStyle)) {
            MarkdownContent(elements = element.elements)
        }
    }
}
