package dev.henkle.markdown

import dev.henkle.markdown.model.MarkdownNode

interface Parser {
    fun parse(markdown: String): List<MarkdownNode>
    fun reset()
}
