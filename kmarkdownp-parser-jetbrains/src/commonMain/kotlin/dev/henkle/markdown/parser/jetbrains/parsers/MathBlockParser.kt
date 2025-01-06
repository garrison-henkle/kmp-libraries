package dev.henkle.markdown.parser.jetbrains.parsers

import dev.henkle.markdown.parser.jetbrains.IRGenerator
import dev.henkle.markdown.model.MarkdownNode
import dev.henkle.markdown.parser.jetbrains.ext.forEach
import dev.henkle.markdown.parser.jetbrains.ext.isEOL
import org.intellij.markdown.ast.ASTNode

internal fun ASTNode.parseAsMathBlock(generator: IRGenerator) {
    val startIndex = if (children.getOrNull(1)?.type?.isEOL == true) 2 else 1
    val endIndex = if (children.getOrNull((children.lastIndex - 1).coerceIn(children.indices))?.type?.isEOL == true) {
        children.lastIndex - 1
    } else {
        children.lastIndex
    }
    val equation = StringBuilder().apply {
        children.forEach(startIndex..<endIndex) { child ->
            append(generator.getText(node = child))
        }
    }.toString()
    generator.output += MarkdownNode.MathBlock(
        raw = generator.getTextString(node = this),
        equation = equation,
    )
}