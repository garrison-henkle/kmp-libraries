package dev.henkle.markdown.parser.jetbrains

import dev.henkle.markdown.Parser
import dev.henkle.markdown.model.MarkdownNode
import dev.henkle.markdown.parser.jetbrains.ext.print
import org.intellij.markdown.parser.MarkdownParser

class JetbrainsParser(private val flavor: MarkdownFlavor = MarkdownFlavor.GitHub) : Parser {
    private val parser by lazy { MarkdownParser(flavour = flavor.descriptor) }

    override fun parse(markdown: String): List<MarkdownNode> {
        val rootASTNode = parser.buildMarkdownTreeFromString(text = markdown)
        rootASTNode.print(fullText = markdown)
        return IRGenerator(text = markdown, syntaxParser = parser).parse(node = rootASTNode)
    }

    override fun reset() = Unit
}
