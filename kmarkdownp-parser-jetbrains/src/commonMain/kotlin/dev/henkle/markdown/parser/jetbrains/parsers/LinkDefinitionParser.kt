package dev.henkle.markdown.parser.jetbrains.parsers

import dev.henkle.markdown.parser.jetbrains.IRGenerator
import dev.henkle.markdown.model.MarkdownNode
import dev.henkle.markdown.parser.jetbrains.ext.isLinkDestination
import dev.henkle.markdown.parser.jetbrains.ext.isLinkLabel
import dev.henkle.markdown.parser.jetbrains.ext.isLinkTitle
import org.intellij.markdown.ast.ASTNode

internal fun ASTNode.parseAsLinkDefinition(generator: IRGenerator) {
    val label = children.firstOrNull { it.type.isLinkLabel }
        ?.let { node ->
            val raw = generator.getTextString(node = node)
            val labelText = raw
                .let { text -> if (text.isNotEmpty()) text.substring(1, text.lastIndex) else text }
            // The Markdown processor doesn't seem to process markdown inside of link definitions' link labels, but
            // it does process them for inline links and link references ¯\_(ツ)_/¯
            // Might need to parse this label as if it is a separate markdown string eventually, so I'll leave the original
            // code commented out below.
            raw to listOf(MarkdownNode.Text(raw = raw, text = labelText))
//            val labelParser = IntermediateParser(text = labelText, fullText = parser.fullText, isLinkParser = true)
//            labelParser.parse(node = node)
        }
    val destination = children.firstOrNull { it.type.isLinkDestination }
        ?.let { node -> generator.getTextString(node = node) }
    val title = children.firstOrNull { it.type.isLinkTitle }
        ?.let { node ->
            val titleParser = generator.createSubParser(node = node, isLinkParser = true)
            node.children.forEach { child ->
                titleParser.parse(node = child, depth = 1)
            }
            titleParser.commitTextBuffer()
            titleParser.output
        }
    if (label != null && destination != null) {
        val (labelRaw, labelText) = label
        generator.output += MarkdownNode.LinkDefinition(
            raw = generator.getTextString(node = this),
            labelRaw = labelRaw,
            label = labelText,
            url = destination,
            title = title,
        )
    }
}
