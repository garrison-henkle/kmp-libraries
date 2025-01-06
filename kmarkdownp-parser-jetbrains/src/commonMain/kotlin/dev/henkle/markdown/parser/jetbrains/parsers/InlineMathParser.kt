package dev.henkle.markdown.parser.jetbrains.parsers

import dev.henkle.markdown.parser.jetbrains.IRGenerator
import dev.henkle.markdown.model.MarkdownNode
import dev.henkle.markdown.parser.jetbrains.ext.forEach
import dev.henkle.markdown.parser.jetbrains.ext.getText
import org.intellij.markdown.ast.ASTNode

internal fun ASTNode.parseAsInlineMath(generator: IRGenerator, depth: Int) {
    if (generator.isSubParser && !generator.isListParser) {
        children.forEach(1..<children.lastIndex) { child ->
            generator.parse(node = child, depth = depth + 1)
        }
    } else {
        val equationParser = generator.createSubParser(node = this, isMathParser = true)
        val equation = equationParser.parse(node = this).getText()
        generator.output += MarkdownNode.InlineMath(
            raw = equationParser.text,
            equation = equation,
        )
    }
}
