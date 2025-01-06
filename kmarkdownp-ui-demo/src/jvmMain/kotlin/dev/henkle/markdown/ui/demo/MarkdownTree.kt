package dev.henkle.markdown.ui.demo

import io.github.treesitter.ktreesitter.InputEdit
import io.github.treesitter.ktreesitter.Node
import io.github.treesitter.ktreesitter.Tree

interface MarkdownTree {
    val blockTree: Tree
    val inlineTrees: List<Tree>
    val inlineIndices: Map<ULong, Int>

    val text: String?
    val textUTF8Bytes: ByteArray?

    /**
     * Edit the syntax tree to keep it in sync
     * with source code that has been modified.
     */
    fun edit(edit: InputEdit)

    /** Create a new tree cursor starting from the node of the tree. */
    fun walk(): MarkdownCursor

    /** Returns the inline tree for this node if the node contains an inline tree */
    fun getInlineTree(parent: Node): Tree?
}
