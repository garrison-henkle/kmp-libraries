package dev.henkle.markdown.parser.jetbrains.parsers

import dev.henkle.markdown.parser.jetbrains.IRGenerator
import dev.henkle.markdown.parser.jetbrains.ext.forEach
import org.intellij.markdown.ast.ASTNode

internal fun ASTNode.parseAsItalics(generator: IRGenerator, depth: Int) {
    children.forEach(1..<children.lastIndex) { child ->
        generator.parse(node = child, depth = depth + 1)
    }
}
