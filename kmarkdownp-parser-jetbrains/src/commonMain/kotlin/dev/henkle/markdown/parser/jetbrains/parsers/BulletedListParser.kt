package dev.henkle.markdown.parser.jetbrains.parsers

import dev.henkle.markdown.parser.jetbrains.IRGenerator
import dev.henkle.markdown.model.MarkdownNode
import dev.henkle.markdown.parser.jetbrains.ext.isListBullet
import dev.henkle.markdown.parser.jetbrains.ext.isListItem
import dev.henkle.markdown.parser.jetbrains.ext.level
import org.intellij.markdown.ast.ASTNode

internal fun ASTNode.parseAsBulletedList(generator: IRGenerator, listLevelOffset: Int) {
    val bullets = children
        .filter { it.type.isListItem }
        .mapNotNull { child ->
            child.children
                .firstOrNull { it.type.isListBullet }
                ?.let { node -> generator.getTextString(node = node).split('-', '*', '+').firstOrNull() }
                ?.level
                ?.let { level ->
                    val content = generator.createSubParser(node = child, isListParser = true)
                        .parse(node = child)
                    MarkdownNode.BulletedList.Item(content = content, level = level + listLevelOffset)
                }
        }

    if (bullets.isNotEmpty()) {
        generator.output += MarkdownNode.BulletedList(
            raw = generator.getTextString(node = this),
            items = bullets,
        )
    }
}
