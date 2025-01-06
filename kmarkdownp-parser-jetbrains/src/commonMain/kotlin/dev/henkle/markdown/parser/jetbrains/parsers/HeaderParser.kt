package dev.henkle.markdown.parser.jetbrains.parsers

import dev.henkle.markdown.parser.jetbrains.IRGenerator
import dev.henkle.markdown.model.MarkdownNode
import dev.henkle.markdown.parser.jetbrains.ext.isH1
import dev.henkle.markdown.parser.jetbrains.ext.isH2
import dev.henkle.markdown.parser.jetbrains.ext.isH3
import dev.henkle.markdown.parser.jetbrains.ext.isH4
import dev.henkle.markdown.parser.jetbrains.ext.isH5
import dev.henkle.markdown.parser.jetbrains.ext.isHeaderContent
import org.intellij.markdown.ast.ASTNode

internal fun ASTNode.parseAsHeader(generator: IRGenerator) {
    children
        .firstOrNull { it.type.isHeaderContent }
        ?.let { node ->
            val text = generator.getTextString(node = node)
            generator.createSubParser(text = text).parse(node = node) to text
        }?.takeIf { it.first.isNotEmpty() }?.also { (content, raw) ->
            generator.output += when {
                type.isH1 -> MarkdownNode.H1(raw = raw, content = content)
                type.isH2 -> MarkdownNode.H2(raw = raw, content = content)
                type.isH3 -> MarkdownNode.H3(raw = raw, content = content)
                type.isH4 -> MarkdownNode.H4(raw = raw, content = content)
                type.isH5 -> MarkdownNode.H5(raw = raw, content = content)
                else -> MarkdownNode.H6(raw = raw, content = content)
            }
    }
}
