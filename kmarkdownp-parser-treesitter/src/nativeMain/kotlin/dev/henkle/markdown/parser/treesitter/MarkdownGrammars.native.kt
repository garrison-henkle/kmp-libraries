package dev.henkle.markdown.parser.treesitter

import dev.henkle.markdown.parser.treesitter.native.tree_sitter_markdown
import dev.henkle.markdown.parser.treesitter.native.tree_sitter_markdown_inline
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
actual class MarkdownGrammars {
    actual fun blockLanguagePointer(): Any = tree_sitter_markdown()?.rawValue?.toLong() ?: 0L
    actual fun inlineLanguagePointer(): Any = tree_sitter_markdown_inline()?.rawValue?.toLong() ?: 0L
}
