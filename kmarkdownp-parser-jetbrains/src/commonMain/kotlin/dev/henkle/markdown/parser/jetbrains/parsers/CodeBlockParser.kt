package dev.henkle.markdown.parser.jetbrains.parsers

import dev.henkle.markdown.parser.jetbrains.IRGenerator
import dev.henkle.markdown.model.MarkdownNode
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode

internal fun ASTNode.parseAsCodeBlock(generator: IRGenerator) {
    val code = StringBuilder()
    var prefixLength = 0
    children.forEach { child ->
        when (child.type) {
            MarkdownTokenTypes.CODE_LINE -> {
                val text = generator.getText(node = child)
                if (prefixLength == 0) {
                    text
                        .indexOfFirst { !it.isWhitespace() }
                        .takeIf { it != -1 }
                        ?.also { i -> prefixLength = i }
                }
                code.append(text.substring(startIndex = prefixLength))
            }
            MarkdownTokenTypes.EOL -> code.appendLine()
        }
    }
    if (code.isNotBlank()) {
        generator.output += MarkdownNode.CodeBlock(
            raw = generator.getTextString(node = this),
            code = code.toString(), language = null)
    }
}
