package dev.henkle.markdown.parser.jetbrains.ext

import dev.henkle.markdown.model.MarkdownNode

fun List<MarkdownNode>.getText(): String = StringBuilder().apply {
    this@getText.forEach { node ->
        when (node) {
            is MarkdownNode.Text -> append(node.text)
            is MarkdownNode.H1 -> append(node.content.getText())
            is MarkdownNode.H2 -> append(node.content.getText())
            is MarkdownNode.H3 -> append(node.content.getText())
            is MarkdownNode.H4 -> append(node.content.getText())
            is MarkdownNode.H5 -> append(node.content.getText())
            is MarkdownNode.H6 -> append(node.content.getText())
            is MarkdownNode.Blockquote -> append(node.content.getText())
            is MarkdownNode.SETextH1 -> append(node.content.getText())
            is MarkdownNode.SETextH2 -> append(node.content.getText())
            is MarkdownNode.CodeBlock -> append(node.code)
            is MarkdownNode.InlineCode -> append(node.code)
            is MarkdownNode.HTMLBlock -> append(node.html)
            is MarkdownNode.HTMLTag -> append(node.html)
            is MarkdownNode.LinkDefinition -> append(node.label)
            is MarkdownNode.LinkReference -> append(node.label)
            is MarkdownNode.InlineLink -> append(node.label)
            is MarkdownNode.Image -> append(node.label)
            is MarkdownNode.InlineMath -> append(node.equation)
            is MarkdownNode.MathBlock -> append(node.equation)
            is MarkdownNode.BulletedList -> {
                node.items.joinToString(separator = "\n") {
                    "${" ".repeat(n = it.level)}- ${it.content.getText()}"
                }
            }
            is MarkdownNode.NumberedList -> {
                node.items.joinToString(separator = "\n") {
                    "${" ".repeat(n = it.level)}${it.number}. ${it.content.getText()}"
                }
            }
            is MarkdownNode.Divider,
            is MarkdownNode.LineBreak,
            // TODO(garrison): add a way to represent a table as a string
            is MarkdownNode.Table -> { /* no-op*/ }
        }
    }
}.toString()
