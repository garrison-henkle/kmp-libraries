package dev.henkle.datastructures.radixtree

/**
 * The default implementation of [RadixTreeEdge] that
 * represents an edge within a [RadixTreeImpl].
 */
internal data class RadixTreeEdgeImpl<T: Any>(
    override var label: String,
    override val target: RadixTreeNodeImpl<T>,
) : RadixTreeEdge<T, RadixTreeEdgeImpl<T>, RadixTreeNodeImpl<T>>
