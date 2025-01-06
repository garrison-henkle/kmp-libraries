package dev.henkle.markdown.parser.jetbrains.parsers

import dev.henkle.markdown.parser.jetbrains.IRGenerator
import dev.henkle.markdown.model.MarkdownNode
import dev.henkle.markdown.parser.jetbrains.ext.isInlineLink
import org.intellij.markdown.ast.ASTNode

internal fun ASTNode.parseAsImage(generator: IRGenerator) {
    children.firstOrNull { it.type.isInlineLink }
        ?.run { parseInlineLinkData(generator = generator) }
        ?.also {
            generator.output += MarkdownNode.Image(
                raw = generator.getTextString(node = this),
                label = it.label,
                url = it.url,
                title = it.title,
            )
        }
}
