package dev.henkle.markdown.parser.treesitter

expect class MarkdownGrammars {
    fun blockLanguagePointer(): Any
    fun inlineLanguagePointer(): Any
}
