package dev.henkle.datastructures.radixtree

/**
 * A representation of a node within a [RadixTree].
 */
interface RadixTreeNode<T: Any, N: RadixTreeNode<T, N, E>, E: RadixTreeEdge<T, E, N>> {
    /**
     * The value of this node.
     *
     * When non-null, this node represents the end of a key.
     *
     * When null, this node represents a fork from which two keys
     * who previously shared a prefix now diverge.
     */
    var value: T?

    /**
     * Indicates whether this node represents a key in the tree
     */
    val isTerminal: Boolean

    /**
     * Retrieves an edge originating at this node with the first
     * character [firstChar], if one exists
     *
     * @param firstChar the first character of the edge's label
     *
     * @return an edge with a label whose first character is [firstChar]
     * or null if no such edge exists
     */
    fun getEdge(firstChar: Char): E?

    /**
     * Adds an edge with an explicit label and target node.
     *
     * This can only be used if the inserted edge is known to be valid
     * ahead of time.
     */
    fun addEdge(label: String, target: N)

    /**
     * Adds an edge to this node or one of its descendants or returns an
     * existing edge if one already exists.
     *
     * Unlike the alternate [addEdge] overload, this method can be used
     * when the proper location of the label is not known.
     *
     * @param label The "search query". It is the entire key that is being inserted into the tree
     * @param value The element that is being inserted into the tree, if this edge is terminal
     * @param start The index within [label] that determines where the current segment starts
     * @param end The index within [label] that determines where the current segment ends, exclusive
     *
     * @return the edge that represents the provided [label]
     */
    fun addEdge(
        label: String,
        value: T?,
        start: Int = 0,
        end: Int = label.length,
    ): E

    /**
     * Removes a matching edge from this node or one of its descendants if one is found.
     *
     * @param parentEdge The edge that terminates at the current node
     * @param label The "search query". It is the entire key that is being removed from the tree
     * @param start The index within [label] that determines where the current segment starts
     * @param end The index within [label] that determines where the current segment ends, exclusive
     *
     * @return the removed edge or null if no edge was removed
     */
    fun removeEdge(
        label: String,
        start: Int = 0,
        end: Int = label.length,
        parentEdge: E? = null,
    ): E?

    /**
     * Prints a string representation of this node and its
     * children to stdout.
     *
     * This should not be directly called by consumers. Please
     * use [RadixTree.print] instead.
     *
     * @param depth the current depth of the node being printed
     * @param terminalChar a suffix that will be applied to a
     * node's label when that node is terminal
     */
    fun print(depth: Int = 0, terminalChar: Char = ']')
}
