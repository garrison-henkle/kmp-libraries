package dev.henkle.markdown.ui.demo

import io.github.treesitter.ktreesitter.Language
import io.github.treesitter.ktreesitter.Node
import io.github.treesitter.ktreesitter.Point
import io.github.treesitter.ktreesitter.TreeCursor

interface MarkdownCursor {
    val markdownTree: MarkdownTree
    val blockCursor: TreeCursor
    val inlineCursor: TreeCursor?
    val isInline: Boolean

    fun moveToInlineTree(): Boolean
    fun moveToBlockTree()

    /** The current node of the cursor. */
    val currentNode: Node

    /**
     * The field ID of the tree cursor's current node, or `0`.
     *
     * @see [Node.childByFieldId]
     * @see [Language.fieldIdForName]
     */
    val currentFieldId: UShort

    /**
     * The field name of the tree cursor's current node, if available.
     *
     * @see [Node.childByFieldName]
     */
    val currentFieldName: String?

   /**
    * The text of the current node
    */
    val currentText: String?
        get() = markdownTree.textUTF8Bytes?.let { bytes ->
            val first = currentNode.startByte.toInt()
            val last = currentNode.endByte.toInt()
            ByteArray(size = last - first) { i -> bytes[first + i] }.toString(charset = Charsets.UTF_8)
        }

    /**
     * Move the cursor to the first child of its current node.
     *
     * @return
     *  `true` if the cursor successfully moved,
     *  or `false` if there were no children.
     */
    fun gotoFirstChild(): Boolean

    /**
     * Move the cursor to the parent of its current node.
     *
     * @return
     *  `true` if the cursor successfully moved,
     *  or `false` if there was no parent node.
     */
    fun gotoParent(): Boolean

    /**
     * Move the cursor to the next sibling of its current node.
     *
     * @return
     *  `true` if the cursor successfully moved,
     *  or `false` if there was no next sibling node.
     */
    fun gotoNextSibling(): Boolean

    /**
     * Move the cursor to the first child of its current
     * node that extends beyond the given byte offset.
     *
     * @return The index of the child node, or `null` if no such child was found.
     */
    fun gotoFirstChildForByte(byte: UInt): UInt?

    /**
     * Move the cursor to the first child of its current
     * node that extends beyond the given point offset.
     *
     * @return The index of the child node, or `null` if no such child was found.
     */
    fun gotoFirstChildForPoint(point: Point): UInt?
}
