package dev.henkle.markdown.parser.jetbrains.ext

import org.intellij.markdown.IElementType
import org.intellij.markdown.ast.ASTNode

internal fun ASTNode.childType(index: Int): IElementType? = children.getOrNull(index = index)?.type
