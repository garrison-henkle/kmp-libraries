package dev.henkle.markdown.ui.demo

import dev.henkle.markdown.ui.grammars.TreeSitterMarkdown
import io.github.treesitter.ktreesitter.Language
import io.github.treesitter.ktreesitter.Node
import io.github.treesitter.ktreesitter.Parser
import io.github.treesitter.ktreesitter.Range
import io.github.treesitter.ktreesitter.Tree

class MarkdownParser(
    val blockLanguage: Language = Language(language = TreeSitterMarkdown.blockLanguagePointer()),
    val inlineLanguage: Language = Language(language = TreeSitterMarkdown.inlineLanguagePointer()),
    configureParser: Parser.() -> Unit = {},
) {
    private val parser: Parser = Parser().apply(configureParser)

    @Throws(IllegalArgumentException::class)
    fun parse(source: String, oldTree: MarkdownTree? = null): MarkdownTree? = try {
        // reset parser to parse the entire input range
        parser.includedRanges = emptyList()
        parser.language = blockLanguage

        val blockTree = parser.parse(source = source, oldTree = oldTree?.blockTree)

        val inlineTrees = mutableListOf<Tree>()
        val inlineIndices = mutableMapOf<ULong, Int>()

        parser.language = inlineLanguage

        val treeCursor = blockTree.walk()

        var i = 0
        outer@while(true) {
            var node: Node
            while(true) {
                if (treeCursor.currentNode.type.isInline || !treeCursor.gotoFirstChild()) {
                    while (!treeCursor.gotoNextSibling()) {
                        if (!treeCursor.gotoParent()) break@outer
                    }
                }
                if (treeCursor.currentNode.type.isInline) {
                    node = treeCursor.currentNode
                    break
                }
            }
            var range = node.range
            val ranges = mutableListOf<Range>()
            if (treeCursor.gotoFirstChild()) {
                while (treeCursor.gotoNextSibling()) {
                    if (!treeCursor.currentNode.isNamed) continue
                    val childRange = treeCursor.currentNode.range
                    ranges += Range(
                        startByte = range.startByte,
                        startPoint = range.startPoint,
                        endByte = childRange.startByte,
                        endPoint = childRange.startPoint,
                    )
                    range = range.copy(
                        startByte = childRange.endByte,
                        startPoint = childRange.endPoint,
                    )
                }
                treeCursor.gotoParent()
            }
            ranges += range
            parser.includedRanges = ranges
            val inlineTree = parser.parse(
                source = source,
                oldTree = oldTree?.inlineTrees?.get(index = i)
            )
            inlineTrees += inlineTree
            inlineIndices[node.id] = i
            i += 1
        }

        MarkdownTreeImpl(
            blockTree = blockTree,
            inlineTrees = inlineTrees,
            inlineIndices = inlineIndices,
        )
    } catch (ex: IllegalStateException) {
        System.err.println("Ex while parsing markdown tree: $ex")
        null
    }
}
