package dev.henkle.markdown.ui.demo

import io.github.treesitter.ktreesitter.InputEdit
import io.github.treesitter.ktreesitter.Node
import io.github.treesitter.ktreesitter.Tree

class MarkdownTreeImpl(
    override val blockTree: Tree,
    override val inlineTrees: List<Tree>,
    override val inlineIndices: Map<ULong, Int>,
) : MarkdownTree {
    override var text: String? = blockTree.text()?.toString()
        private set

    override var textUTF8Bytes: ByteArray? = text?.encodeToByteArray()
        private set

    override fun getInlineTree(parent: Node): Tree? =
        inlineIndices[parent.id]?.let { treeIndex -> inlineTrees[treeIndex] }

    override fun edit(edit: InputEdit) {
        blockTree.edit(edit = edit)
        for (inlineTree in inlineTrees) {
            inlineTree.edit(edit = edit)
        }
        blockTree.text()?.toString()?.also { text ->
            this.text = text
            textUTF8Bytes = text.encodeToByteArray()
        } ?: run {
            text = null
            textUTF8Bytes = null
        }
    }

    override fun walk(): MarkdownCursor =
        MarkdownCursorImpl(
            markdownTree = this,
            blockCursor = blockTree.walk(),
        )
}
