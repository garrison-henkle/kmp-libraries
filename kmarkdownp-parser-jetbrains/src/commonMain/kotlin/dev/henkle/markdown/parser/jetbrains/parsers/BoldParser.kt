package dev.henkle.markdown.parser.jetbrains.parsers

import dev.henkle.markdown.parser.jetbrains.IRGenerator
import dev.henkle.markdown.parser.jetbrains.ext.forEach
import org.intellij.markdown.ast.ASTNode

internal fun ASTNode.parseAsBold(generator: IRGenerator, depth: Int) {
    children.forEach(2..<(children.lastIndex - 1)) { child ->
        generator.parse(node = child, depth = depth + 1)
    }
}
