package dev.henkle.markdown.ui.generator

import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import dev.henkle.markdown.model.MarkdownNode
import dev.henkle.markdown.ui.model.InlineUIElement
import dev.henkle.markdown.ui.model.UIElement
import kotlinx.atomicfu.atomic

typealias Url = String
typealias Label = String

class UIIRGenerator {
    private val idCounter = atomic(initial = 0)
    private fun getId(): String = idCounter.getAndIncrement().toString()

    fun process(nodes: List<MarkdownNode>): IRGenerationResult {
        val elements = mutableListOf<UIElement>()
        val inlineContent = mutableListOf<InlineUIElement>()
        val urls = mutableMapOf<Label, Url>()
        var annotatedStringStart: Int? = null
        val setAnnotatedStringStart: (Int?) -> Unit = {
            annotatedStringStart = it
        }

        nodes.forEachIndexed { i, node ->
            when (node) {
                is MarkdownNode.Text -> onTextNode(i, annotatedStringStart, setAnnotatedStringStart)
                is MarkdownNode.HTMLTag -> onTextNode(i, annotatedStringStart, setAnnotatedStringStart)
                is MarkdownNode.InlineCode -> onTextNode(i, annotatedStringStart, setAnnotatedStringStart)
                is MarkdownNode.InlineMath -> onTextNode(i, annotatedStringStart, setAnnotatedStringStart)
                is MarkdownNode.InlineLink -> onTextNode(i, annotatedStringStart, setAnnotatedStringStart)
                is MarkdownNode.LinkReference -> onTextNode(i, annotatedStringStart, setAnnotatedStringStart)

                is MarkdownNode.LinkDefinition -> {
                    onNonTextNode(nodes, elements, inlineContent, urls, i, annotatedStringStart, setAnnotatedStringStart)
                    val labelResult = process(nodes = node.label)
                    urls += labelResult.urls
                    inlineContent += labelResult.inlineContent
                    val titleElements = node.title?.let { titleNodes ->
                        val titleResult = process(nodes = titleNodes)
                        urls += titleResult.urls
                        inlineContent += titleResult.inlineContent
                        titleResult.elements
                    }
                    elements += UIElement.LinkDefinition(
                        id = getId(),
                        labelRaw = node.labelRaw,
                        label = labelResult.elements,
                        url = node.url,
                        title = titleElements,
                    )
                }
                is MarkdownNode.Divider -> {
                    onNonTextNode(nodes, elements, inlineContent, urls, i, annotatedStringStart, setAnnotatedStringStart)
                    elements += UIElement.Divider(id = getId())
                }
                is MarkdownNode.Blockquote -> {
                    onNonTextNode(nodes, elements, inlineContent, urls, i, annotatedStringStart, setAnnotatedStringStart)
                    val result = process(nodes = node.content)
                    urls += result.urls
                    inlineContent += result.inlineContent
                    elements += UIElement.Blockquote(id = getId(), elements = result.elements)
                }
                is MarkdownNode.CodeBlock -> {
                    onNonTextNode(nodes, elements, inlineContent, urls, i, annotatedStringStart, setAnnotatedStringStart)
                    elements += UIElement.CodeBlock(id = getId(), code = node.code, language = node.language)
                }
                is MarkdownNode.HTMLBlock -> {
                    onNonTextNode(nodes, elements, inlineContent, urls, i, annotatedStringStart, setAnnotatedStringStart)
                    elements += UIElement.CodeBlock(id = getId(), code = node.html, language = "html")
                }
                is MarkdownNode.MathBlock -> {
                    onNonTextNode(nodes, elements, inlineContent, urls, i, annotatedStringStart, setAnnotatedStringStart)
                    elements += UIElement.MathBlock(id = getId(), equation = node.equation)
                }
                is MarkdownNode.LineBreak -> {
                    onNonTextNode(nodes, elements, inlineContent, urls, i, annotatedStringStart, setAnnotatedStringStart)
                    elements += UIElement.LineBreak(id = getId(), newlineCount = node.raw.length)
                }
                is MarkdownNode.H1 -> {
                    onNonTextNode(nodes, elements, inlineContent, urls, i, annotatedStringStart, setAnnotatedStringStart)
                    val result = process(nodes = node.content)
                    urls += result.urls
                    inlineContent += result.inlineContent
                    elements += UIElement.H1(id = getId(), elements = result.elements)
                }
                is MarkdownNode.H2 -> {
                    onNonTextNode(nodes, elements, inlineContent, urls, i, annotatedStringStart, setAnnotatedStringStart)
                    val result = process(nodes = node.content)
                    urls += result.urls
                    inlineContent += result.inlineContent
                    elements += UIElement.H2(id = getId(), elements = result.elements)
                }
                is MarkdownNode.H3 -> {
                    onNonTextNode(nodes, elements, inlineContent, urls, i, annotatedStringStart, setAnnotatedStringStart)
                    val result = process(nodes = node.content)
                    urls += result.urls
                    inlineContent += result.inlineContent
                    elements += UIElement.H3(id = getId(), elements = result.elements)
                }
                is MarkdownNode.H4 -> {
                    onNonTextNode(nodes, elements, inlineContent, urls, i, annotatedStringStart, setAnnotatedStringStart)
                    val result = process(nodes = node.content)
                    urls += result.urls
                    inlineContent += result.inlineContent
                    elements += UIElement.H4(id = getId(), elements = result.elements)
                }
                is MarkdownNode.H5 -> {
                    onNonTextNode(nodes, elements, inlineContent, urls, i, annotatedStringStart, setAnnotatedStringStart)
                    val result = process(nodes = node.content)
                    urls += result.urls
                    inlineContent += result.inlineContent
                    elements += UIElement.H5(id = getId(), elements = result.elements)
                }
                is MarkdownNode.H6 -> {
                    onNonTextNode(nodes, elements, inlineContent, urls, i, annotatedStringStart, setAnnotatedStringStart)
                    val result = process(nodes = node.content)
                    urls += result.urls
                    inlineContent += result.inlineContent
                    elements += UIElement.H6(id = getId(), elements = result.elements)
                }
                is MarkdownNode.SETextH1 -> {
                    onNonTextNode(nodes, elements, inlineContent, urls, i, annotatedStringStart, setAnnotatedStringStart)
                    val result = process(nodes = node.content)
                    urls += result.urls
                    inlineContent += result.inlineContent
                    elements += UIElement.SETextH1(id = getId(), elements = result.elements)
                }
                is MarkdownNode.SETextH2 -> {
                    onNonTextNode(nodes, elements, inlineContent, urls, i, annotatedStringStart, setAnnotatedStringStart)
                    val result = process(nodes = node.content)
                    urls += result.urls
                    inlineContent += result.inlineContent
                    elements += UIElement.SETextH2(id = getId(), elements = result.elements)
                }
                is MarkdownNode.Image -> {
                    onNonTextNode(nodes, elements, inlineContent, urls, i, annotatedStringStart, setAnnotatedStringStart)
                    elements += UIElement.Image(
                        id = getId(),
                        label = buildAnnotatedString(
                            nodes = node.label,
                            inlineContent = inlineContent,
                            urls = urls,
                        ),
                        url = node.url,
                        title = node.title,
                    )
                }
                is MarkdownNode.BulletedList -> {
                    onNonTextNode(nodes, elements, inlineContent, urls, i, annotatedStringStart, setAnnotatedStringStart)
                    elements += UIElement.BulletedList(
                        id = getId(),
                        items = node.items.map { item ->
                            val result = process(nodes = item.content)
                            urls += result.urls
                            inlineContent += result.inlineContent
                            UIElement.BulletedList.Item(
                                content = result.elements,
                                level = item.level,
                            )
                        },
                    )
                }
                is MarkdownNode.NumberedList -> {
                    onNonTextNode(nodes, elements, inlineContent, urls, i, annotatedStringStart, setAnnotatedStringStart)
                    elements += UIElement.NumberedList(
                        id = getId(),
                        items = node.items.map { item ->
                            val result = process(nodes = item.content)
                            urls += result.urls
                            inlineContent += result.inlineContent
                            UIElement.NumberedList.Item(
                                content = result.elements,
                                level = item.level,
                                number = item.number,
                            )
                        },
                    )
                }
                is MarkdownNode.Table -> {
                    onNonTextNode(nodes, elements, inlineContent, urls, i, annotatedStringStart, setAnnotatedStringStart)
                    elements += UIElement.Table(
                        id = getId(),
                        columns = node.columns.map {
                            UIElement.Table.Column(
                                alignment = when (it.alignment) {
                                    MarkdownNode.Table.ColumnAlignment.Left -> UIElement.Table.Alignment.Left
                                    MarkdownNode.Table.ColumnAlignment.Center -> UIElement.Table.Alignment.Center
                                    MarkdownNode.Table.ColumnAlignment.Right -> UIElement.Table.Alignment.Right
                                },
                            )
                        },
                        cells = node.cells.map { row ->
                            row.map { cell ->
                                val result = process(nodes = cell.content)
                                urls += result.urls
                                inlineContent += result.inlineContent
                                UIElement.Table.Cell(content = result.elements)
                            }
                        },
                        hasHeader = node.hasHeader,
                    )
                }
            }
        }
        annotatedStringStart?.also { start ->
            val text = buildAnnotatedString(
                nodes = nodes,
                inlineContent = inlineContent,
                urls = urls,
                range = start..<nodes.size,
            )
            elements += UIElement.Text(id = getId(), text = text)
        }
        return IRGenerationResult(
            elements = elements,
            inlineContent = inlineContent,
            urls = urls,
        )
    }

    private inline fun onNonTextNode(
        nodes: List<MarkdownNode>,
        elements: MutableList<UIElement>,
        inlineContent: MutableList<InlineUIElement>,
        urls: MutableMap<Label, Url>,
        i: Int,
        annotatedStringStart: Int? = null,
        setAnnotatedStringStart: (Int?) -> Unit,
    ) {
        if (annotatedStringStart != null) {
            val text = buildAnnotatedString(
                nodes = nodes,
                inlineContent = inlineContent,
                urls = urls,
                range = annotatedStringStart..<i,
            )
            elements += UIElement.Text(id = getId(), text = text)
            setAnnotatedStringStart(null)
        }
    }

    private inline fun onTextNode(
        i: Int,
        annotatedStringStart: Int? = null,
        setAnnotatedStringStart: (Int?) -> Unit,
    ) {
        if (annotatedStringStart == null) {
            setAnnotatedStringStart(i)
        }
    }

    private fun buildAnnotatedString(
        nodes: List<MarkdownNode>,
        inlineContent: MutableList<InlineUIElement>,
        urls: MutableMap<Label, Url>,
        range: IntRange = nodes.indices,
    ): AnnotatedString {
        val builder = AnnotatedString.Builder()
        for (i in range) {
            when (val node = nodes[i]) {
                is MarkdownNode.Text -> {
                    val offset = builder.length
                    builder.append(text = node.text)
                    for (annotation in node.annotations) {
                        builder.addStyle(
                            style = SpanStyle(
                                fontWeight = if (annotation.isBold) FontWeight.Bold else FontWeight.Normal,
                                fontStyle = if (annotation.isItalic) FontStyle.Italic else FontStyle.Normal,
                            ),
                            start = offset + annotation.start,
                            end = offset + annotation.endExclusive,
                        )
                    }
                }
                is MarkdownNode.HTMLTag -> {
                    // TODO(garrison): maybe this should be a code span?
                    builder.append(text = node.html)
                }
                is MarkdownNode.InlineCode -> {
                    val element = InlineUIElement.Code(id = getId(), code = node.code)
                    inlineContent += element
                    builder.appendInlineContent(id = element.id)

                }
                is MarkdownNode.InlineMath -> {
                    // TODO(garrison)
                    val element = InlineUIElement.Math(id = getId(), equation = node.equation)
                    inlineContent += element
                    builder.appendInlineContent(id = element.id)
                }
                is MarkdownNode.InlineLink -> {
                    val labelResult = process(nodes = node.label)
                    urls += labelResult.urls
                    inlineContent += labelResult.inlineContent
                    val element = InlineUIElement.Link(
                        id = getId(),
                        labelRaw = node.labelRaw,
                        label = labelResult.elements,
                        title = node.title?.let { title ->
                            listOf(UIElement.Text(id = getId(), text = AnnotatedString(text = title)))
                        },
                    )
                    inlineContent += element
                    urls += node.labelRaw to node.url
                    builder.appendInlineContent(id = element.id)
                }
                is MarkdownNode.LinkReference -> {
                    val labelResult = process(nodes = node.label)
                    urls += labelResult.urls
                    inlineContent += labelResult.inlineContent
                    val element = InlineUIElement.Link(
                        id = getId(),
                        labelRaw = node.labelRaw,
                        label = labelResult.elements,
                        title = node.title?.let { titleNodes ->
                            val result = process(nodes = titleNodes)
                            urls += result.urls
                            inlineContent += result.inlineContent
                            result.elements
                        },
                    )
                    inlineContent += element
                    builder.appendInlineContent(id = element.id)
                }

                is MarkdownNode.LineBreak,
                is MarkdownNode.LinkDefinition,
                is MarkdownNode.Divider,
                is MarkdownNode.Blockquote,
                is MarkdownNode.CodeBlock,
                is MarkdownNode.HTMLBlock,
                is MarkdownNode.MathBlock,
                is MarkdownNode.H1,
                is MarkdownNode.H2,
                is MarkdownNode.H3,
                is MarkdownNode.H4,
                is MarkdownNode.H5,
                is MarkdownNode.H6,
                is MarkdownNode.SETextH1,
                is MarkdownNode.SETextH2,
                is MarkdownNode.BulletedList,
                is MarkdownNode.NumberedList,
                is MarkdownNode.Image,
                is MarkdownNode.Table -> {
                    return builder.toAnnotatedString()
                }
            }
        }
        return builder.toAnnotatedString()
    }

    data class IRGenerationResult(
        val elements: List<UIElement>,
        val urls: Map<Label, Url>,
        val inlineContent: List<InlineUIElement>,
    )
}
