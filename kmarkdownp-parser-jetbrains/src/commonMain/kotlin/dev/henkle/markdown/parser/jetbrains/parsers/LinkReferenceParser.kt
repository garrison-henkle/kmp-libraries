package dev.henkle.markdown.parser.jetbrains.parsers

import dev.henkle.markdown.parser.jetbrains.IRGenerator
import dev.henkle.markdown.model.MarkdownNode
import dev.henkle.markdown.parser.jetbrains.ext.forEach
import dev.henkle.markdown.parser.jetbrains.ext.isLinkLabel
import dev.henkle.markdown.parser.jetbrains.ext.isLinkText
import org.intellij.markdown.ast.ASTNode

internal fun ASTNode.parseAsLinkReference(generator: IRGenerator) {
    val labelTitle = children.firstOrNull { it.type.isLinkText }?.let { node ->
        val textParser = generator.createSubParser(node = node, isLinkParser = true)
        node.children.forEach(1..<node.children.lastIndex) { child ->
            textParser.parse(node = child, depth = 1)
        }
        textParser.commitTextBuffer()
        textParser.output.takeIf { it.isNotEmpty() }
    }
    val labelContent = children.firstOrNull { it.type.isLinkLabel }?.let { node ->
        val labelRaw = generator.getTextString(node = node)
        labelRaw to if (generator.isLinkParser) {
            listOf(MarkdownNode.Text(raw = labelRaw, text = labelRaw))
        } else {
            val labelParser = generator.createSubParser(text = labelRaw, isLinkParser = true)
            node.children.forEach(1..<node.children.lastIndex) { child ->
                labelParser.parse(node = child, depth = 1)
            }
            labelParser.commitTextBuffer()
            labelParser.output
        }
    }
    if (labelContent != null && labelContent.second.isNotEmpty()) {
        val (labelRaw, labelText) = labelContent
        generator.output += MarkdownNode.LinkReference(
            raw = generator.getTextString(node = this),
            labelRaw = labelRaw,
            label = labelText,
            title = labelTitle,
        )
    }
}
