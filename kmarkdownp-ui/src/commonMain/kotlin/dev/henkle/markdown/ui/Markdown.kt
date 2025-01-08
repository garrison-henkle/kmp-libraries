package dev.henkle.markdown.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import co.touchlab.kermit.Logger
import dev.henkle.markdown.ui.components.MarkdownInlineContent
import dev.henkle.markdown.ui.generator.UIIRGenerator
import dev.henkle.markdown.ui.model.InlineUIElement
import dev.henkle.markdown.ui.model.UIElement
import dev.henkle.markdown.ui.utils.LocalMarkdownStyle
import dev.henkle.markdown.ui.utils.LocalMarkdownUIComponents
import dev.henkle.markdown.ui.utils.ProvideMarkdownInlineContent
import dev.henkle.markdown.ui.utils.ProvideMarkdownLinkHandler
import dev.henkle.markdown.ui.utils.ProvideMarkdownStyle
import dev.henkle.markdown.ui.utils.ProvideMarkdownUIComponents
import dev.henkle.markdown.ui.utils.ProvideMarkdownUrls
import dev.henkle.markdown.ui.utils.ext.getPlaceholderAlignment

@Composable
fun Markdown(
    modifier: Modifier = Modifier,
    markdown: String,
    parser: KMarkdownPUI,
    components: MarkdownUIComponents = MarkdownUIComponents(),
    style: MarkdownStyle = MarkdownStyle(),
    getInlineContentAlignment: (element: InlineUIElement) -> PlaceholderVerticalAlign? = { null },
    useLazyColumn: Boolean = true,
    spacing: Dp = Dp.Unspecified,
    linkHandler: (label: String, url: String) -> Unit,
) {
    val parsedMarkdown = remember(parser, markdown) {
        parser.process(markdown = markdown)
    }

    Markdown(
        modifier = modifier,
        markdown = parsedMarkdown,
        components = components,
        style = style,
        getInlineContentAlignment = getInlineContentAlignment,
        useLazyColumn = useLazyColumn,
        spacing = spacing,
        linkHandler = linkHandler,
    )
}

@Composable
fun Markdown(
    modifier: Modifier = Modifier,
    markdown: UIIRGenerator.IRGenerationResult,
    components: MarkdownUIComponents = MarkdownUIComponents(),
    style: MarkdownStyle = MarkdownStyle(),
    getInlineContentAlignment: (element: InlineUIElement) -> PlaceholderVerticalAlign? = { null },
    useLazyColumn: Boolean = true,
    spacing: Dp = Dp.Unspecified,
    linkHandler: (label: String, url: String) -> Unit,
) {
    ProvideMarkdownUIComponents(components = components) {
        ProvideMarkdownStyle(style = style) {
            ProvideMarkdownUrls(urls = markdown.urls) {
                ProvideMarkdownLinkHandler(handler = linkHandler) {
                    ProvideMarkdownInlineContent(inlineContent = emptyMap()) {
                        InlineMarkdownMeasurer(
                            modifier = modifier,
                            inlineElements = markdown.inlineContent,
                            getInlineContentAlignment = getInlineContentAlignment,
                        ) { measurements ->
                            val inlineContent = remember(measurements) {
                                markdown.inlineContent.associate { element ->
                                    val (measurement, alignment) = measurements[element.id]
                                        ?: (TextUnitSize() to PlaceholderVerticalAlign.Center)
                                    element.id to InlineTextContent(
                                        placeholder = Placeholder(
                                            width = measurement.width,
                                            height = measurement.height,
                                            placeholderVerticalAlign = alignment,
                                        ),
                                        children = { MarkdownInlineContent(element = element) }
                                    )
                                }
                            }
                            ProvideMarkdownInlineContent(inlineContent = inlineContent) {
                                MarkdownContent(
                                    modifier = modifier,
                                    elements = markdown.elements,
                                    useLazyColumn = useLazyColumn,
                                    spacing = spacing,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LazyListScope.Markdown(
    modifier: Modifier = Modifier,
    markdown: UIIRGenerator.IRGenerationResult,
    components: MarkdownUIComponents = MarkdownUIComponents(),
    style: MarkdownStyle = MarkdownStyle(),
    getInlineContentAlignment: (element: InlineUIElement) -> PlaceholderVerticalAlign? = { null },
    spacing: Dp = Dp.Unspecified,
    linkHandler: (label: String, url: String) -> Unit,
) {
    ProvideMarkdownUIComponents(components = components) {
        ProvideMarkdownStyle(style = style) {
            ProvideMarkdownUrls(urls = markdown.urls) {
                ProvideMarkdownLinkHandler(handler = linkHandler) {
                    ProvideMarkdownInlineContent(inlineContent = emptyMap()) {
                        InlineMarkdownMeasurer(
                            modifier = modifier,
                            inlineElements = markdown.inlineContent,
                            getInlineContentAlignment = getInlineContentAlignment,
                        ) { measurements ->
                            val inlineContent = remember(measurements) {
                                markdown.inlineContent.associate { element ->
                                    val (measurement, alignment) = measurements[element.id]
                                        ?: (TextUnitSize() to PlaceholderVerticalAlign.Center)
                                    element.id to InlineTextContent(
                                        placeholder = Placeholder(
                                            width = measurement.width,
                                            height = measurement.height,
                                            placeholderVerticalAlign = alignment,
                                        ),
                                        children = { MarkdownInlineContent(element = element) }
                                    )
                                }
                            }
                            ProvideMarkdownInlineContent(inlineContent = inlineContent) {
                                MarkdownContent(
                                    modifier = modifier,
                                    elements = markdown.elements,
                                    spacing = spacing,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MarkdownContent(
    modifier: Modifier = Modifier,
    elements: List<UIElement>,
    spacing: Dp = Dp.Unspecified,
    useLazyColumn: Boolean = false,
) {
    if (useLazyColumn) {
        LazyColumn(modifier = modifier) {
            MarkdownContent(elements = elements, spacing = spacing)
        }
    } else {
        Column(modifier = modifier) {
            elements.forEachIndexed { i, element ->
                MarkdownItem(element = element)
                if (spacing != Dp.Unspecified && i != elements.lastIndex) {
                    Spacer(modifier = Modifier.height(height = spacing))
                }
            }
        }
    }
}

fun LazyListScope.MarkdownContent(
    elements: List<UIElement>,
    spacing: Dp = Dp.Unspecified,
) {
    itemsIndexed(items = elements, key = { _, element -> element.id }) { i, element ->
        MarkdownItem(element = element)
        if (spacing != Dp.Unspecified && i != elements.lastIndex) {
            Spacer(modifier = Modifier.height(height = spacing))
        }
    }
}

@Composable
private fun MarkdownItem(element: UIElement) {
    val components = LocalMarkdownUIComponents.current
    when (element) {
        is UIElement.Text -> components.text(element)
        is UIElement.LineBreak -> components.lineBreak(element)
        is UIElement.Blockquote -> components.blockquote(element)
        is UIElement.CodeBlock -> components.codeBlock(element)
        is UIElement.MathBlock -> { components.mathBlock(element) }
        is UIElement.Divider -> components.divider(element)
        is UIElement.H1 -> components.h1(element)
        is UIElement.H2 -> components.h2(element)
        is UIElement.H3 -> components.h3(element)
        is UIElement.H4 -> components.h4(element)
        is UIElement.H5 -> components.h5(element)
        is UIElement.H6 -> components.h6(element)
        is UIElement.SETextH1 -> components.seTextH1(element)
        is UIElement.SETextH2 -> components.seTextH2(element)
        is UIElement.LinkDefinition -> components.linkDefinition(element)
        is UIElement.Image -> components.image(element)
        is UIElement.BulletedList -> components.bulletedList(element)
        is UIElement.NumberedList -> components.numberedList(element)
        is UIElement.Table -> components.table(element)
    }
}

private data class TextUnitSize(val width: TextUnit = 0.sp, val height: TextUnit = 0.sp)

@Composable
private fun InlineMarkdownMeasurer(
    modifier: Modifier = Modifier,
    inlineElements: List<InlineUIElement>,
    getInlineContentAlignment: (element: InlineUIElement) -> PlaceholderVerticalAlign?,
    content: @Composable (
        measurements: Map<String, Pair<TextUnitSize, PlaceholderVerticalAlign>>,
    ) -> Unit,
) {
    val style = LocalMarkdownStyle.current
    SubcomposeLayout(modifier = modifier) { constraints ->
        val measurements = inlineElements.associate { element ->
            subcompose(slotId = element.id) {
                MarkdownInlineContent(element = element)
            }.getOrNull(0)?.measure(constraints = constraints)?.run {
                element.id to (
                    TextUnitSize(
                        width = width.toSp(),
                        height = height.toSp(),
                    ) to (getInlineContentAlignment(element) ?: style.getPlaceholderAlignment(element = element))
                )
            } ?: throw Exception("You can not use an empty Composable for MarkdownInlineContent!")
        }

        val placeable = subcompose(slotId = "content") {
            content(measurements)
        }[0].measure(constraints = constraints)

        layout(width = placeable.width, height = placeable.height) {
            placeable.place(x = 0, y = 0)
        }
    }
}
