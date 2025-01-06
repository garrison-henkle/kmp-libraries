package dev.henkle.markdown.parser.jetbrains.parsers

import dev.henkle.markdown.parser.jetbrains.IRGenerator
import dev.henkle.markdown.model.MarkdownNode
import dev.henkle.markdown.parser.jetbrains.ext.forEach
import dev.henkle.markdown.parser.jetbrains.ext.isLinkDestination
import dev.henkle.markdown.parser.jetbrains.ext.isLinkText
import dev.henkle.markdown.parser.jetbrains.ext.isLinkTitle
import org.intellij.markdown.ast.ASTNode

internal fun ASTNode.parseAsInlineLink(generator: IRGenerator) {
    parseInlineLinkData(generator = generator)?.also { link ->
        generator.output += link
    }
}

internal fun ASTNode.parseInlineLinkData(generator: IRGenerator): MarkdownNode.InlineLink? {
    val label = children
        .firstOrNull { it.type.isLinkText }
        ?.let { node ->
            val labelParser = generator.createSubParser(node = node, isLinkParser = true)
            node.children.forEach(1..<node.children.lastIndex) { child ->
                labelParser.parse(node = child, depth = 1)
            }
            labelParser.commitTextBuffer()
            labelParser.text to labelParser.output
        }
    val url = children.firstOrNull { it.type.isLinkDestination }
        ?.let { destination -> generator.getTextString(destination) }
    val title = children.firstOrNull { it.type.isLinkTitle }
        ?.let { title -> generator.getTextString(node = title) }
        ?.takeIf { it.isNotEmpty() }
        ?.let { text -> text.substring(1..<text.lastIndex) }
    return label?.let { (labelRaw, label) ->
        url?.let { linkUrl ->
            MarkdownNode.InlineLink(
                raw = generator.getTextString(node = this),
                labelRaw = labelRaw,
                label = label,
                url = linkUrl,
                title = title,
            )
        }
    }
}
