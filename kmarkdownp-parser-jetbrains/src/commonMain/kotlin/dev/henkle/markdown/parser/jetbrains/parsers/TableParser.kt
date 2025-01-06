package dev.henkle.markdown.parser.jetbrains.parsers

import dev.henkle.markdown.parser.jetbrains.IRGenerator
import dev.henkle.markdown.model.MarkdownNode
import dev.henkle.markdown.parser.jetbrains.ext.forEach
import dev.henkle.markdown.parser.jetbrains.ext.isTableCell
import dev.henkle.markdown.parser.jetbrains.ext.isTableHeader
import dev.henkle.markdown.parser.jetbrains.ext.isTableRow
import dev.henkle.markdown.parser.jetbrains.ext.isTableSeparator
import dev.henkle.markdown.parser.jetbrains.ext.isWhitespace
import org.intellij.markdown.ast.ASTNode

internal fun ASTNode.parseAsTable(generator: IRGenerator) {
    children
        .firstOrNull { it.type.isTableSeparator }
        ?.parseSeparator(generator = generator)
        .also { columns ->
            val hasHeader = children.any { it.type.isTableHeader }
            val cells = children
                .filter { it.type.isTableRow || it.type.isTableHeader }
                .map { child -> child.parseRow(generator = generator) }
            generator.output += MarkdownNode.Table(
                raw = generator.getTextString(node = this),
                columns = columns
                    ?: List(size = cells.firstOrNull()?.size ?: 0) {
                        MarkdownNode.Table.ColumnDefinition(alignment = MarkdownNode.Table.ColumnAlignment.Left)
                    },
                cells = cells,
                hasHeader = hasHeader,
            )
        }
    }

private fun ASTNode.parseRow(generator: IRGenerator): List<MarkdownNode.Table.Cell> =
    children
        .filter { it.type.isTableCell }
        .map { child -> child.parseCell(generator = generator) }

private fun ASTNode.parseCell(generator: IRGenerator): MarkdownNode.Table.Cell {
    val startIndex = if (children.firstOrNull()?.type?.isWhitespace == true) 1 else 0
    val endIndex = if (children.lastOrNull()?.type?.isWhitespace == true) children.lastIndex else children.size
    val cellParser = generator.createSubParser(node = this)
    children.forEach(startIndex..<endIndex) { child ->
        cellParser.parse(node = child, depth = 1)
    }
    cellParser.commitTextBuffer()
    return MarkdownNode.Table.Cell(content = cellParser.output)
}

private fun ASTNode.parseSeparator(generator: IRGenerator): List<MarkdownNode.Table.ColumnDefinition> {
    val text = generator.getTextString(node = this)
    val columns = text.split('|')
        .filter { it.isNotBlank() }
        .map { separator ->
            val trimmed = separator.trim()
            val alignLeft = trimmed.first() == ':'
            val alignRight = trimmed.last() == ':'
            val alignment = when {
                alignLeft && alignRight -> MarkdownNode.Table.ColumnAlignment.Center
                alignRight -> MarkdownNode.Table.ColumnAlignment.Right
                else -> MarkdownNode.Table.ColumnAlignment.Left
            }
            MarkdownNode.Table.ColumnDefinition(alignment = alignment)
        }
    return columns
}
