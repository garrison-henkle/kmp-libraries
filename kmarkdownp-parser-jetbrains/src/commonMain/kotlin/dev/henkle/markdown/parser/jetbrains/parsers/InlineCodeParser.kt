package dev.henkle.markdown.parser.jetbrains.parsers

import dev.henkle.markdown.parser.jetbrains.IRGenerator
import dev.henkle.markdown.model.MarkdownNode
import dev.henkle.markdown.parser.jetbrains.ext.childType
import dev.henkle.markdown.parser.jetbrains.ext.forEach
import dev.henkle.markdown.parser.jetbrains.ext.isBacktick
import dev.henkle.markdown.parser.jetbrains.ext.isEOL
import dev.henkle.markdown.parser.jetbrains.ext.isHtmlTag
import dev.henkle.markdown.parser.jetbrains.ext.isText
import dev.henkle.markdown.parser.jetbrains.ext.isTextChar
import dev.henkle.markdown.parser.jetbrains.ext.isWhitespace
import org.intellij.markdown.ast.ASTNode

internal fun ASTNode.parseAsCodeSpan(generator: IRGenerator) {
    val code = StringBuilder()

    val firstIndex = if (childType(index = 1)?.isWhitespace == true) 2 else 1
    val lastIndex = if(childType(index = children.lastIndex - 1)?.isWhitespace == true) {
        children.lastIndex - 1
    } else {
        children.lastIndex
    }

    children.forEach(firstIndex..<lastIndex) { child ->
        when {
            child.type.isBacktick -> generator.getAndAppendTextOf(node = child, to = code)
            child.type.isText -> generator.getAndAppendTextOf(node = child, to = code)
            child.type.isTextChar -> generator.getAndAppendTextOf(node = child, to = code)
            child.type.isHtmlTag -> generator.getAndAppendTextOf(node = child, to = code)
            child.type.isEOL -> code.append(' ')
        }
    }

    if (code.isNotEmpty()) {
        generator.output += MarkdownNode.InlineCode(
            raw = generator.getTextString(node = this),
            code = code.toString(),
        )
    }
}
