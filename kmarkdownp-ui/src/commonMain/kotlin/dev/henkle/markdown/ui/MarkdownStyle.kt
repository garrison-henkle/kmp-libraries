package dev.henkle.markdown.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.henkle.markdown.ui.model.Padding
import dev.henkle.markdown.ui.model.style.BlockquoteStyle
import dev.henkle.markdown.ui.model.style.CodeBlockStyle
import dev.henkle.markdown.ui.model.style.DividerStyle
import dev.henkle.markdown.ui.model.style.ImageStyle
import dev.henkle.markdown.ui.model.style.InlineCodeStyle
import dev.henkle.markdown.ui.model.style.InlineLinkStyle
import dev.henkle.markdown.ui.model.style.InlineMathStyle
import dev.henkle.markdown.ui.model.style.LineBreakStyle
import dev.henkle.markdown.ui.model.style.LinkDefinitionStyle
import dev.henkle.markdown.ui.model.style.LinkStyle
import dev.henkle.markdown.ui.model.style.ListStyle
import dev.henkle.markdown.ui.model.style.MathBlockStyle
import dev.henkle.markdown.ui.model.style.SETextHeaderStyle
import dev.henkle.markdown.ui.model.style.TableStyle
import dev.henkle.markdown.ui.utils.bottomBorder
import dev.henkle.markdown.ui.utils.leftBorder
import dev.henkle.markdown.ui.utils.rightBorder
import dev.henkle.markdown.ui.utils.topBorder

data class MarkdownStyle(
    val text: TextStyle = TextStyle(),
    val lineBreak: LineBreakStyle = LineBreakStyle(newlineHeight = 10.dp),
    val inlineCode: InlineCodeStyle = InlineCodeStyle(
        textStyle = text.copy(fontFamily = FontFamily.Monospace),
        border = null,
        borderColor = Color.Unspecified,
        inlineAlignment = PlaceholderVerticalAlign.Center,
    ),
    val inlineMath: InlineMathStyle = InlineMathStyle(
        textStyle = text.copy(fontFamily = FontFamily.Monospace),
        inlineAlignment = PlaceholderVerticalAlign.AboveBaseline,
    ),
    val inlineLink: InlineLinkStyle = InlineLinkStyle(
        linkStyle = LinkStyle(
            textStyle = text.copy(
                color = Color(0xff121212),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
            ),
            backgroundColor = Color(0xffeeeef1),
            margin = Padding(horizontal = 1.5.dp, vertical = 2.dp),
            padding = Padding(horizontal = 5.dp, vertical = 3.dp),
        ),
        inlineAlignment = PlaceholderVerticalAlign.Center,
    ),
    val blockquote: BlockquoteStyle = BlockquoteStyle(
        textStyle = text,
        quoteBarStartMargin = 5.dp,
        quoteBarEndMargin = 5.dp,
        quoteBarWidth = 4.dp,
        quoteBarShape = RoundedCornerShape(percent = 50),
        quoteBarColor = Color.Gray,
    ),
    val codeBlock: CodeBlockStyle = CodeBlockStyle(
        textStyle = text.copy(fontFamily = FontFamily.Monospace, color = Color.White),
        backgroundColor = Color(0xff161b22),
        verticalPadding = 5.dp,
        horizontalPadding = 10.dp,
    ),
    val divider: DividerStyle = DividerStyle(
        color = Color(0xb330363d),
        thickness = 1.dp,
    ),
    val h1: TextStyle = text.copy(fontSize = 40.sp, fontWeight = FontWeight.Bold),
    val h2: TextStyle = h1.copy(fontSize = 36.sp),
    val h3: TextStyle = h1.copy(fontSize = 32.sp),
    val h4: TextStyle = h1.copy(fontSize = 28.sp),
    val h5: TextStyle = h1.copy(fontSize = 24.sp),
    val h6: TextStyle = h1.copy(fontSize = 20.sp),
    val seTextH1: SETextHeaderStyle = SETextHeaderStyle(
        textStyle = h1,
        dividerStyle = divider,
    ),
    val seTextH2: SETextHeaderStyle = SETextHeaderStyle(
        textStyle = h2,
        dividerStyle = divider,
    ),
    val linkDefinition: LinkDefinitionStyle = LinkDefinitionStyle(
        linkStyle = inlineLink.linkStyle,
        contentStyle = text.copy(color = Color(0xff4493f8)),
        rowAlignment = Alignment.CenterVertically,
        spacing = 5.dp,
    ),
    val image: ImageStyle = ImageStyle(
        width = Dp.Infinity,
        height = Dp.Unspecified,
        contentScale = ContentScale.FillWidth,
        alignment = Alignment.CenterStart,
    ),
    val list: ListStyle = ListStyle(
        contentAlignment = Alignment.CenterVertically,
        markerStyle = text,
        markerStartMargin = 5.dp,
        markerEndMargin = 5.dp,
        markerAlignment = Alignment.Top,
        levelIndentation = 5.dp,
        bulletChar = '•',
        numberChar = '.',
        itemSpacing = 5.dp,
    ),
    val table: TableStyle = TableStyle(
        textStyle = text,
        imageStyle = ImageStyle(
            width = 400.dp,
            height = 400.dp,
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center,
        ),
        cellMeasurementPadding = { _, _ -> Padding(all = 4.dp) },
        cellModifier = { x, y, width, height, tableHasHeader ->
            Modifier
                .fillMaxSize()
                .border(width = Dp.Hairline, color = Color.Black)
                .run {
                    // borders between cells double up, so this doubles up the table's outer borders
                    var modifier = this
                    if (x == 0) modifier = modifier.leftBorder()
                    if (x == width - 1) modifier = modifier.rightBorder()
                    if (y == 0) modifier = modifier.topBorder()
                    if (y == height - 1) modifier = modifier.bottomBorder()
                    modifier
                }.run {
                    // header is darker gray, if present, and all other rows alternate a lighter gray and white
                    when {
                        y == 0 && tableHasHeader -> background(color = Color(0xffbdbdbd))
                        y % 2 == 0 -> background(color = Color(0xffe0e0e0))
                        else -> background(color = Color.White)
                    }
                }.padding(all = 4.dp)
                .wrapContentSize(align = Alignment.TopStart)
        },
    ),
    val mathBlock: MathBlockStyle = MathBlockStyle(
        textStyle = codeBlock.textStyle,
        backgroundColor = codeBlock.backgroundColor,
        verticalPadding = codeBlock.verticalPadding,
        horizontalPadding = codeBlock.horizontalPadding,
    ),
)
