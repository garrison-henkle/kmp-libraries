package dev.henkle.markdown.parser.treesitter

actual class MarkdownGrammars {
    actual fun blockLanguagePointer(): Any = treeSitterMarkdown()
    actual fun inlineLanguagePointer(): Any = treeSitterMarkdownInline()

    companion object {
        init {
            loadGrammarCLibrary()
        }

        @JvmStatic
        private external fun treeSitterMarkdown(): Long

        @JvmStatic
        private external fun treeSitterMarkdownInline(): Long
    }
}
