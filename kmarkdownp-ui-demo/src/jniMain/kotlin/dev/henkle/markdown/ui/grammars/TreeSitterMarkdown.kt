package dev.henkle.markdown.ui.grammars

object TreeSitterMarkdown : Grammar {
    override val sharedLibraryName: String = "markdown-grammar"

    init {
        try {
            System.loadLibrary(sharedLibraryName)
        } catch (ex: UnsatisfiedLinkError) {
            @Suppress("UnsafeDynamicallyLoadedCode")
            System.load(copyLibToTmpAndGetPath() ?: throw ex)
        }
    }

    fun blockLanguagePointer(): Any = treeSitterMarkdown()

    fun inlineLanguagePointer(): Any = treeSitterMarkdownInline()

    @JvmStatic
    private external fun treeSitterMarkdown(): Long

    @JvmStatic
    private external fun treeSitterMarkdownInline(): Long
}
