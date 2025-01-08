package dev.henkle.markdown.ui.components.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import dev.henkle.markdown.ui.MarkdownContent
import dev.henkle.markdown.ui.model.UIElement
import dev.henkle.markdown.ui.model.style.LinkStyle
import dev.henkle.markdown.ui.recomposeHighlighter
import dev.henkle.markdown.ui.utils.LocalMarkdownLinkHandler
import dev.henkle.markdown.ui.utils.LocalMarkdownStyle
import dev.henkle.markdown.ui.utils.LocalMarkdownUrls
import dev.henkle.markdown.ui.utils.ProvideMarkdownStyle
import dev.henkle.markdown.ui.utils.ext.getText
import dev.henkle.markdown.ui.utils.ext.padding
import kotlin.math.max

@Composable
fun MarkdownLink(
    modifier: Modifier = Modifier,
    title: List<UIElement>?,
    label: List<UIElement>,
    labelRaw: String,
    style: LinkStyle,
) {
    val markdownStyle = LocalMarkdownStyle.current
    val urls = LocalMarkdownUrls.current
    val linkHandler = LocalMarkdownLinkHandler.current
    val density = LocalDensity.current
    val measurer = rememberTextMeasurer()
    val (linkWidth, linkHeight) = remember(measurer, title, label, style, density) {
        with (density) {
            val textSize = measurer.measure(text = (title ?: label).getText(), style = style.textStyle).size
            val circleWidth = textSize.width.toSp().value + (style.padding.start + style.padding.end).toSp().value
            val circleHeight = textSize.height.toSp().value + (style.padding.top + style.padding.bottom).toSp().value
            max(circleWidth, circleHeight).sp.toDp() to circleHeight.sp.toDp()
        }
    }
    Box(
        modifier = modifier
            .recomposeHighlighter()
            .padding(values = style.margin)
            .width(width = linkWidth)
            .height(height = linkHeight)
            .clip(shape = CircleShape)
            .background(color = style.backgroundColor)
            .clickable { urls[labelRaw]?.also { url -> linkHandler(label.getText(), url) } }
            // TODO(garrison): below is a bug. The padding needs to be added to the width and height
            //  above to ensure this padding doesn't clip the content
            .padding(values = style.padding),
        contentAlignment = Alignment.Center,
    ) {
        ProvideMarkdownStyle(style = markdownStyle.copy(text = style.textStyle)) {
            MarkdownContent(modifier = Modifier.recomposeHighlighter(), elements = title ?: label)
        }
    }
}
