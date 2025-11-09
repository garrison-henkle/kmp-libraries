package dev.henkle.compose.ktab.model

/**
 * A node within the tab system.
 *
 * @see SplitNode
 * @see LeafNode
 */
sealed class PaneNode<T: Tab<T>> {
    /**
     * The id of the node
     */
    abstract val id: NodeId

    /**
     * The parent of the node or null if it has no parent
     */
    abstract var parent: SplitNode<T>?
        internal set
}
