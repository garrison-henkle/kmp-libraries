package dev.henkle.markdown.ui

import dev.henkle.markdown.ui.components.MarkdownBlockquote
import dev.henkle.markdown.ui.components.MarkdownCodeBlock
import dev.henkle.markdown.ui.components.MarkdownDivider
import dev.henkle.markdown.ui.components.MarkdownHeader
import dev.henkle.markdown.ui.components.MarkdownImage
import dev.henkle.markdown.ui.components.MarkdownInlineCode
import dev.henkle.markdown.ui.components.MarkdownInlineLink
import dev.henkle.markdown.ui.components.MarkdownInlineMath
import dev.henkle.markdown.ui.components.MarkdownLineBreak
import dev.henkle.markdown.ui.components.MarkdownLinkDefinition
import dev.henkle.markdown.ui.components.MarkdownList
import dev.henkle.markdown.ui.components.MarkdownMathBlock
import dev.henkle.markdown.ui.components.MarkdownSETextHeader
import dev.henkle.markdown.ui.components.MarkdownTable
import dev.henkle.markdown.ui.components.MarkdownText
import dev.henkle.markdown.ui.model.InlineUIElement
import dev.henkle.markdown.ui.model.UIComponent
import dev.henkle.markdown.ui.model.UIElement

data class MarkdownUIComponents(
    val text: UIComponent<UIElement.Text> =
        { element -> MarkdownText(element = element) },
    val lineBreak: UIComponent<UIElement.LineBreak> =
        { element -> MarkdownLineBreak(element = element) },
    val inlineCode: UIComponent<InlineUIElement.Code> =
        { element -> MarkdownInlineCode(element = element) },
    val inlineMath: UIComponent<InlineUIElement.Math> =
        { element -> MarkdownInlineMath(element = element) },
    val inlineLink: UIComponent<InlineUIElement.Link> =
        { element -> MarkdownInlineLink(element = element) },
    val blockquote: UIComponent<UIElement.Blockquote> =
        { element -> MarkdownBlockquote(element = element) },
    val codeBlock: UIComponent<UIElement.CodeBlock> =
        { element -> MarkdownCodeBlock(element = element) },
    val divider: UIComponent<UIElement.Divider> =
        { element -> MarkdownDivider(element = element) },
    val h1: UIComponent<UIElement.H1> =
        { element -> MarkdownHeader(element = element) },
    val h2: UIComponent<UIElement.H2> =
        { element -> MarkdownHeader(element = element) },
    val h3: UIComponent<UIElement.H3> =
        { element -> MarkdownHeader(element = element) },
    val h4: UIComponent<UIElement.H4> =
        { element -> MarkdownHeader(element = element) },
    val h5: UIComponent<UIElement.H5> =
        { element -> MarkdownHeader(element = element) },
    val h6: UIComponent<UIElement.H6> =
        { element -> MarkdownHeader(element = element) },
    val seTextH1: UIComponent<UIElement.SETextH1> =
        { element -> MarkdownSETextHeader(element = element) },
    val seTextH2: UIComponent<UIElement.SETextH2> =
        { element -> MarkdownSETextHeader(element = element) },
    val linkDefinition: UIComponent<UIElement.LinkDefinition> =
        { element -> MarkdownLinkDefinition(element = element) },
    val image: UIComponent<UIElement.Image> =
        { element -> MarkdownImage(element = element) },
    val numberedList: UIComponent<UIElement.NumberedList> =
        { element -> MarkdownList(element = element) },
    val bulletedList: UIComponent<UIElement.BulletedList> =
        { element -> MarkdownList(element = element) },
    val table: UIComponent<UIElement.Table> =
        { element -> MarkdownTable(element = element) },
    val mathBlock: UIComponent<UIElement.MathBlock> =
        { element -> MarkdownMathBlock(element = element) }
)
