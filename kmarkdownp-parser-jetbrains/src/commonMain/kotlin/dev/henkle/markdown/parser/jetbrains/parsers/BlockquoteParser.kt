package dev.henkle.markdown.parser.jetbrains.parsers

import dev.henkle.markdown.parser.jetbrains.IRGenerator
import dev.henkle.markdown.model.MarkdownNode
import org.intellij.markdown.ast.ASTNode

internal fun ASTNode.parseAsBlockquote(generator: IRGenerator) {
    val blockquoteParser = generator.createSubParser(node = this, isBlockquoteParser = true)
    children.forEach { child ->
        blockquoteParser.parse(node = child, depth = 1)
    }
    blockquoteParser.commitTextBuffer()
    generator.output += MarkdownNode.Blockquote(
        raw = blockquoteParser.text,
        content = blockquoteParser.output,
    )
}
