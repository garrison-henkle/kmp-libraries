package dev.henkle.markdown.parser.jetbrains.parsers

import dev.henkle.markdown.parser.jetbrains.IRGenerator
import dev.henkle.markdown.model.MarkdownNode
import dev.henkle.markdown.parser.jetbrains.ext.isEqualsLineHeader
import dev.henkle.markdown.parser.jetbrains.ext.isLineHeaderContent
import org.intellij.markdown.ast.ASTNode

internal fun ASTNode.parseAsSETextHeader(generator: IRGenerator) {
    children.firstOrNull { it.type.isLineHeaderContent }
        ?.let { node ->
            val innerText = generator.getTextString(node = node)
            val rootASTNode = generator.syntaxParser.buildMarkdownTreeFromString(text = innerText)
            val content = IRGenerator(text = innerText, syntaxParser = generator.syntaxParser).parse(node = rootASTNode)
            content to innerText
        }?.takeIf { it.first.isNotEmpty() }?.also { (content, raw) ->
            generator.output += when {
                type.isEqualsLineHeader -> MarkdownNode.SETextH1(raw = raw, content = content)
                else -> MarkdownNode.SETextH2(raw = raw, content = content)
            }
        }
}
