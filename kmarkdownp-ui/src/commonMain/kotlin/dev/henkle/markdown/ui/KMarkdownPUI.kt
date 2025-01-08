package dev.henkle.markdown.ui

import co.touchlab.kermit.Logger
import dev.henkle.markdown.Parser
import dev.henkle.markdown.ui.generator.UIIRGenerator

class KMarkdownPUI(private val markdownParser: Parser) {
    fun process(markdown: String): UIIRGenerator.IRGenerationResult {
        val ir = markdownParser.parse(markdown = markdown)
        return UIIRGenerator().process(nodes = ir)
    }
}
