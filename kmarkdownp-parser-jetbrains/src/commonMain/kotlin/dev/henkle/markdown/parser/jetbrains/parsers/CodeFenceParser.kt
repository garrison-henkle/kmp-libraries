package dev.henkle.markdown.parser.jetbrains.parsers

import dev.henkle.markdown.parser.jetbrains.IRGenerator
import dev.henkle.markdown.model.MarkdownNode
import dev.henkle.markdown.parser.jetbrains.ext.childType
import dev.henkle.markdown.parser.jetbrains.ext.forEach
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode

internal fun ASTNode.parseAsCodeFence(generator: IRGenerator) {
    val code = StringBuilder()
    var language: String? = null
    var firstEOL = true
    val lastNodeIsCodeFenceEnd = childType(index = children.lastIndex) == MarkdownTokenTypes.CODE_FENCE_END
    val lastIndex = if (lastNodeIsCodeFenceEnd && childType(index = children.lastIndex - 1) == MarkdownTokenTypes.EOL) {
        children.size - 2
    } else {
        children.size
    }

    children.forEach(0..<lastIndex) continueFor@{ child ->
        when (child.type) {
            MarkdownTokenTypes.CODE_FENCE_START,
            MarkdownTokenTypes.CODE_FENCE_END -> return@continueFor

            MarkdownTokenTypes.CODE_FENCE_CONTENT -> generator.getAndAppendTextOf(node = child, to = code)

            MarkdownTokenTypes.EOL -> {
                if (!firstEOL) {
                    code.appendLine()
                }
                firstEOL = false
            }

            MarkdownTokenTypes.FENCE_LANG -> language = generator.getTextString(node = child)
        }
    }

    if (code.isNotEmpty()) {
        generator.output += MarkdownNode.CodeBlock(
            raw = generator.getTextString(node = this),
            code = code.toString(),
            language = language,
        )
    }
}
