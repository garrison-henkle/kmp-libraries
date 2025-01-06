package dev.henkle.markdown.model

// TODO(garrison): add the raw source text to each MarkdownNode in case the client wants to use it additional processing
//  ex. use-case: give users ability to copy a node
sealed interface MarkdownNode {
    val raw: String

    data class Text(
        override val raw: String,
        val text: String,
        val annotations: List<MarkdownTextAnnotation> = emptyList(),
    ) : MarkdownNode {
        override fun toString(): String =
            "Text(text=${text.replace("\n", " ")}, annotations=$annotations)"
    }
    data class SETextH1(override val raw: String, val content: List<MarkdownNode>) : MarkdownNode {
        override fun toString(): String = "SETextH1(content=$content)"
    }
    data class SETextH2(override val raw: String, val content: List<MarkdownNode>) : MarkdownNode {
        override fun toString(): String = "SETextH2(content=$content)"
    }
    data class H1(override val raw: String, val content: List<MarkdownNode>) : MarkdownNode {
        override fun toString(): String = "H1(content=$content)"
    }
    data class H2(override val raw: String, val content: List<MarkdownNode>) : MarkdownNode {
        override fun toString(): String = "H2(content=$content)"
    }
    data class H3(override val raw: String, val content: List<MarkdownNode>) : MarkdownNode {
        override fun toString(): String = "H3(content=$content)"
    }
    data class H4(override val raw: String, val content: List<MarkdownNode>) : MarkdownNode {
        override fun toString(): String = "H4(content=$content)"
    }
    data class H5(override val raw: String, val content: List<MarkdownNode>) : MarkdownNode {
        override fun toString(): String = "H5(content=$content)"
    }
    data class H6(override val raw: String, val content: List<MarkdownNode>) : MarkdownNode {
        override fun toString(): String = "H6(content=$content)"
    }
    data class Blockquote(override val raw: String, val content: List<MarkdownNode>) : MarkdownNode {
        override fun toString(): String = "Blockquote(content=$content)"
    }
    data class BulletedList(override val raw: String, val items: List<Item>) : MarkdownNode {
        override fun toString(): String = StringBuilder().apply {
            appendLine("BulletedList(")
            appendLine("    items = [")
            for (item in items) {
                appendLine("        $item,")
            }
            appendLine()
            appendLine("    ],")
            append(")")
        }.toString()

        data class Item(val content: List<MarkdownNode>, val level: Int)
    }
    data class NumberedList(override val raw: String, val items: List<Item>) : MarkdownNode {
        override fun toString(): String = StringBuilder().apply {
            appendLine("NumberedList(")
            appendLine("    items = [")
            for (item in items) {
                appendLine("        $item,")
            }
            appendLine("    ],")
            append(")")
        }.toString()

        data class Item(val content: List<MarkdownNode>, val level: Int, val number: Int)
    }
    data class LinkDefinition(
        override val raw: String,
        val labelRaw: String,
        val label: List<MarkdownNode>,
        val url: String,
        val title: List<MarkdownNode>?,
    ) : MarkdownNode {
        override fun toString(): String = "LinkDefinition(label=$label, url=$url, title=$title)"
    }
    data class LinkReference(
        override val raw: String,
        val labelRaw: String,
        val label: List<MarkdownNode>,
        val title: List<MarkdownNode>? = null,
    ) : MarkdownNode {
        override fun toString(): String = "LinkReference(label=$label, title=$title)"
    }
    data class InlineLink(
        override val raw: String,
        val labelRaw: String,
        val label: List<MarkdownNode>,
        val url: String,
        val title: String?,
    ) : MarkdownNode {
        override fun toString(): String = "InlineLink(label=$label, url=$url, title=$title)"
    }
    data class InlineMath(override val raw: String, val equation: String) : MarkdownNode {
        override fun toString(): String = "InlineMath(equation=${equation.replace("\n", " ")})"
    }
    data class MathBlock(override val raw: String, val equation: String) : MarkdownNode {
        override fun toString(): String = "MathBlock(equation=${equation.replace("\n", " ")})"
    }
    data class InlineCode(override val raw: String, val code: String) : MarkdownNode {
        override fun toString(): String = "InlineCode(code=${code.replace("\n", " ")})"
    }
    data class CodeBlock(override val raw: String, val code: String, val language: String?) : MarkdownNode {
        override fun toString(): String = "CodeBlock(code=${code.replace("\n", " ")}, language=$language)"
    }
    data class HTMLBlock(override val raw: String, val html: String) : MarkdownNode {
        override fun toString(): String = "HTMLBlock(html=${html.replace("\n", " ")})"
    }
    data class HTMLTag(override val raw: String, val html: String) : MarkdownNode {
        override fun toString(): String = "HTMLTag(html=${html.replace("\n", " ")})"
    }
    data class Image(override val raw: String, val label: List<MarkdownNode>, val url: String, val title: String?) : MarkdownNode {
        override fun toString(): String = "Image(label=$label, url=$url, title=$title)"
    }
    /**
     * @property columns the definitions of the table columns
     * @property cells a list of columns containing individual cells. Tables are organized into rows of cells
     */
    data class Table(
        override val raw: String,
        val columns: List<ColumnDefinition>,
        val cells: List<List<Cell>>,
        val hasHeader: Boolean,
    ) : MarkdownNode {
        override fun toString(): String = StringBuilder().apply {
            append("Table(\n    columns=$columns,\n    cells=[\n")
            cells.forEach { col ->
                append("        Column(\n")
                col.forEach { cell ->
                    append("            Cell(text=${cell.content}),\n")
                }
                append("        ),\n")
            }
            append("    ],\n    hasHeader=")
            append(hasHeader.toString())
            append(",\n)")
        }.toString()

        enum class ColumnAlignment {
            Left,
            Center,
            Right,
        }
        data class ColumnDefinition(val alignment: ColumnAlignment)
        data class Cell(val content: List<MarkdownNode>)
    }
    data class LineBreak(override val raw: String) : MarkdownNode {
        override fun toString(): String = "LineBreak(newlineCount=${raw.count { it == '\n' }})"
    }
    data class Divider(override val raw: String) : MarkdownNode {
        override fun toString(): String = "Divider"
    }
}
