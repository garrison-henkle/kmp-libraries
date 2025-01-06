package dev.henkle.markdown.parser.jetbrains.parsers

import dev.henkle.markdown.parser.jetbrains.IRGenerator
import dev.henkle.markdown.model.MarkdownNode
import dev.henkle.markdown.model.MarkdownTextAnnotation
import dev.henkle.markdown.parser.jetbrains.ext.flatMapNotNull
import dev.henkle.markdown.parser.jetbrains.ext.isListItem
import dev.henkle.markdown.parser.jetbrains.ext.isListNumber
import dev.henkle.markdown.parser.jetbrains.ext.level
import org.intellij.markdown.ast.ASTNode

internal fun ASTNode.parseAsNumberedList(generator: IRGenerator, listLevelOffset: Int) {
    val listItems = children
        .filter { it.type.isListItem }
        .flatMapNotNull { child ->
            child.children
                .firstOrNull { it.type.isListNumber }
                ?.let { node -> generator.getTextString(node = node).split('.', ')').firstOrNull() }
                ?.let { numberString ->
                    var i = numberString.lastIndex
                    if (i == -1) return@let null
                    while (i >= 0) {
                        // space and tab are below 0x30 (zero character) in ascii
                        if (numberString[i].code < 0x30) {
                            i++
                            break
                        }
                        i--
                    }
                    i = i.coerceIn(numberString.indices)
                    numberString
                        .substring(startIndex = i)
                        .toIntOrNull()
                        ?.let { number -> i to number }
                }?.let { (level, number) ->
                    val currentLevel = level + listLevelOffset
                    val listItems = mutableListOf<MarkdownNode.NumberedList.Item>()
                    val content = generator.createSubParser(node = child, isListParser = true)
                        .parse(node = child)
                    var currentItemContent = mutableListOf<MarkdownNode>()
                    var currentItemContentAlreadyAdded = false
                    var lastTextTrailingLevel: Int? = null
                    for (item in content) {
                        when (item) {
                            is MarkdownNode.Text -> {
                                val subListItem = listNumberRegex.find(item.text)
                                if (subListItem != null) {
                                    val originalItemText = subListItem.groupValues[1]
                                    val newItemLevel = subListItem.groupValues[2].level
                                    val newItemNumber = subListItem.groupValues[3]
                                    val newItemTextRaw = subListItem.groupValues[4]
                                    val newItemText = newItemTextRaw.trimEnd(' ')
                                    val originalItemAnnotations = mutableListOf<MarkdownTextAnnotation>()
                                    val newItemAnnotations = mutableListOf<MarkdownTextAnnotation>()
                                    item.annotations.forEach { annotation ->
                                        when {
                                            annotation.start < originalItemText.length &&
                                            annotation.endExclusive <= originalItemText.length -> {
                                                originalItemAnnotations += annotation
                                            }

                                            annotation.start < originalItemText.length -> {
                                                val originalItemLength = originalItemText.length
                                                originalItemAnnotations += annotation.copy(
                                                    endExclusive = originalItemLength,
                                                )
                                                newItemAnnotations += annotation.copy(
                                                    start = 0,
                                                    endExclusive = annotation.endExclusive - originalItemLength,
                                                )
                                            }

                                            else -> {
                                                val originalItemLength = originalItemText.length
                                                newItemAnnotations += annotation.copy(
                                                    start = annotation.start - originalItemLength,
                                                    endExclusive = annotation.endExclusive - originalItemLength,
                                                )
                                            }
                                        }
                                    }
                                    currentItemContent += MarkdownNode.Text(
                                        text = originalItemText,
                                        raw = originalItemText,
                                        annotations = originalItemAnnotations,
                                    )
                                    listItems += MarkdownNode.NumberedList.Item(
                                        content = currentItemContent,
                                        level = currentLevel,
                                        number = number,
                                    )
                                    currentItemContent = mutableListOf()
                                    currentItemContentAlreadyAdded = true
                                    listItems += MarkdownNode.NumberedList.Item(
                                        content = currentItemContent,
                                        level = newItemLevel + listLevelOffset,
                                        number = newItemNumber.toIntOrNull() ?: 1,
                                    )
                                    lastTextTrailingLevel = newItemTextRaw.level

                                    currentItemContent += MarkdownNode.Text(
                                        text = newItemText,
                                        raw = newItemText,
                                        annotations = newItemAnnotations,
                                    )
                                } else {
                                    lastTextTrailingLevel = null
                                    currentItemContent += item
                                }
                            }
                            is MarkdownNode.NumberedList -> {
                                lastTextTrailingLevel?.also { additionalLevel ->
                                    item.items
                                        .map { it.copy(level = it.level + additionalLevel) }
                                        .forEach {
                                            listItems += it
                                        }
                                }
                            }
                            else -> {
                                lastTextTrailingLevel = null
                                currentItemContent += item
                            }
                        }
                    }
                    if (!currentItemContentAlreadyAdded && currentItemContent.isNotEmpty()) {
                        listItems += MarkdownNode.NumberedList.Item(
                            content = currentItemContent,
                            level = currentLevel,
                            number = number,
                        )
                    }
                    listItems
                }
        }
    if (listItems.isNotEmpty()) {
        generator.output += MarkdownNode.NumberedList(
            raw = generator.getTextString(node = this),
            items = listItems,
        )
    }
}

private val listNumberRegex = """(.+?)([\s\u2005]+)(\d)[.)]\s(.+)""".toRegex()