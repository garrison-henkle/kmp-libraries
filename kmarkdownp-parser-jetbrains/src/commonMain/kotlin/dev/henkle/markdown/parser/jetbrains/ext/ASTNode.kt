package dev.henkle.markdown.parser.jetbrains.ext

import org.intellij.markdown.IElementType
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.getTextInNode

internal fun ASTNode.childType(index: Int): IElementType? = children.getOrNull(index = index)?.type

internal fun ASTNode.print(fullText: String, depth: Int = 0) {
    val prefix = "  ".repeat(n = depth)
    val text = getTextInNode(fullText).toString().replace("\n", "\\n")
    println("$prefix- ${type.name} \"$text\"")
    children.forEach { child ->
        child.print(fullText = fullText, depth = depth + 1)
    }
}
