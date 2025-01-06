package dev.henkle.markdown.parser.jetbrains.parsers

import dev.henkle.markdown.parser.jetbrains.IRGenerator
import dev.henkle.markdown.model.MarkdownNode
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode

internal fun ASTNode.parseAsHTMLBlock(generator: IRGenerator) {
    val builder = StringBuilder()
    children.forEach { child ->
        when (child.type) {
            MarkdownTokenTypes.HTML_BLOCK_CONTENT -> builder.append(generator.getText(node = child))
            MarkdownTokenTypes.EOL -> builder.appendLine()
        }
    }
    generator.output += MarkdownNode.HTMLBlock(
        raw = generator.getTextString(node = this),
        html = builder.toString(),
    )
}
