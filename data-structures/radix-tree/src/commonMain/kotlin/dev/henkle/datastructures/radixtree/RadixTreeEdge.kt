package dev.henkle.datastructures.radixtree

/**
 * A representation of an edge within a [RadixTree]
 */
interface RadixTreeEdge<T : Any, E: RadixTreeEdge<T, E, N>, N: RadixTreeNode<T, N, E>> {
    /**
     * The label associated with this edge
     */
    var label: String

    /**
     * The node that this edge terminates at
     */
    val target: N
}
