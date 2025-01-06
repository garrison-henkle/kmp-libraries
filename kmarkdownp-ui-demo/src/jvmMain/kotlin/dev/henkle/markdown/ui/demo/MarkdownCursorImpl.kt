package dev.henkle.markdown.ui.demo

import io.github.treesitter.ktreesitter.Node
import io.github.treesitter.ktreesitter.Point
import io.github.treesitter.ktreesitter.TreeCursor

class MarkdownCursorImpl(
    override val markdownTree: MarkdownTree,
    override val blockCursor: TreeCursor,
    inlineCursor: TreeCursor? = null,
) : MarkdownCursor {
    override var inlineCursor: TreeCursor? = inlineCursor
        private set

    override val currentNode: Node get() = (inlineCursor ?: blockCursor).currentNode

    override val isInline: Boolean get() = inlineCursor != null

    override val currentFieldId: UShort get() = (inlineCursor ?: blockCursor).currentFieldId

    override val currentFieldName: String? get() = (inlineCursor ?: blockCursor).currentFieldName

    override fun moveToInlineTree(): Boolean {
        val node = blockCursor.currentNode
        // todo(garrison): if this is not working, change it to node.grammarType
        return if (node.type.isInline) {
            markdownTree.getInlineTree(parent = node)
                ?.let { inlineTree ->
                    inlineCursor = inlineTree.walk()
                    true
                } ?: false
        } else {
            false
        }
    }

    override fun moveToBlockTree() {
        inlineCursor = null
    }

    override fun gotoFirstChild(): Boolean =
        inlineCursor?.gotoFirstChild() // ?.also { println("gotoFirstChild in inline tree") }
            ?: run {
                if (moveToInlineTree()) {
//                    println("gotoFirstChild moved into inline tree")
                    // using !! to copy the reference implementation in Rust (which is also unsafe)
                    if (!inlineCursor!!.gotoFirstChild()) {
//                        println("gotoFirstChild moved out of inline tree")
                        moveToBlockTree()
                        false
                    } else {
                        true
                    }
                } else {
//                    println("gotoFirstChild in block tree")
                    blockCursor.gotoFirstChild()
                }
            }

    override fun gotoParent(): Boolean =
        inlineCursor
            ?.let { cursor ->
//                println("gotoParent in inline tree")
                cursor.gotoParent()
                if (cursor.currentNode.parent == null) {
//                    println("gotoParent moved out of inline tree")
                    moveToBlockTree()
                }
                true
            } ?: blockCursor.gotoParent() // .also { println("gotoParent in block tree") }

    override fun gotoNextSibling(): Boolean = inlineCursor?.gotoNextSibling() ?: blockCursor.gotoNextSibling()

    override fun gotoFirstChildForByte(byte: UInt): UInt? =
        inlineCursor?.gotoFirstChildForByte(byte = byte)
            ?: run {
                if (moveToInlineTree()) {
                    // using !! to copy the reference implementation in Rust (which is also unsafe)
                    inlineCursor!!.gotoFirstChildForByte(byte = byte)
                } else {
                    blockCursor.gotoFirstChildForByte(byte = byte)
                }
            }

    override fun gotoFirstChildForPoint(point: Point): UInt? =
        inlineCursor?.gotoFirstChildForPoint(point = point)
            ?: run {
                if (moveToInlineTree()) {
                    // using !! to copy the reference implementation in Rust (which is also unsafe)
                    inlineCursor!!.gotoFirstChildForPoint(point = point)
                } else {
                    blockCursor.gotoFirstChildForPoint(point = point)
                }
            }
}