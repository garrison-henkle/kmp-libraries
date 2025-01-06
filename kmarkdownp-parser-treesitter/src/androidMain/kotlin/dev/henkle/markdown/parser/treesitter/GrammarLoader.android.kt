package dev.henkle.markdown.parser.treesitter

internal actual fun loadGrammarCLibrary() {
    System.loadLibrary("mdgrammars")
}
