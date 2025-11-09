package dev.henkle.datastructures.radixtree

import kotlin.math.min

/**
 * The default implementation of [RadixTreeNode] that
 * represents a node within a [RadixTreeImpl].
 *
 * Contains logic to add and remove edges to other nodes.
 */
internal class RadixTreeNodeImpl<T: Any>(
    override var value: T?,
    isTerminal: Boolean = true,
    private val getKey: (T) -> String,
): RadixTreeNode<T, RadixTreeNodeImpl<T>, RadixTreeEdgeImpl<T>>{
    override var isTerminal: Boolean = isTerminal
        private set

    private var edges: HashMap<Char, RadixTreeEdgeImpl<T>>? = null

    private val edgeCount: Int get() = edges?.count() ?: 0

    override fun getEdge(firstChar: Char): RadixTreeEdgeImpl<T>? = edges?.get(firstChar)

    override fun addEdge(label: String, target: RadixTreeNodeImpl<T>){
        edges.getOrCreate{ edges = it }[label[0]] = RadixTreeEdgeImpl(
            label = label,
            target = target
        )
    }

    override fun addEdge(
        label: String,
        value: T?,
        start: Int,
        end: Int
    ): RadixTreeEdgeImpl<T> {
        val edgeMap = edges.getOrCreate{ edges = it }
        val firstChar = label[start]
        val existingEdge = edgeMap[firstChar]
        return if (existingEdge == null) {
            RadixTreeEdgeImpl<T>(
                label = label.substring(start, end),
                target = RadixTreeNodeImpl(
                    isTerminal = true,
                    value = value,
                    getKey = getKey,
                ),
            ).also{ edgeMap[firstChar] = it }
        } else{
            var splitIndex = -1
            val searchLabelLength = end - start
            for (i in 1 ..< min(searchLabelLength, existingEdge.label.length)) {
                if (existingEdge.label[i] != label[start + i]) {
                    splitIndex = i
                    break
                }
            }
            // if the splitIndex is -1, no forking of an existing edge necessary
            if (splitIndex == -1) {
                when {
                    // if an existing edge exists that shares the same label -> that node's target
                    // will now represent the new value
                    // n.b. I'm choosing to override the value here, so no duplicate labels can
                    // exist -> getKey needs to be a unique mapping
                    searchLabelLength == existingEdge.label.length ->
                        existingEdge.apply {
                            target.apply {
                                isTerminal = true
                                this.value = value
                            }
                        }

                    // if an existing edge has a label that is a prefix of the new label -> a new
                    // edge will extend from that node's target, with the new edge's target
                    // representing the new value
                    searchLabelLength > existingEdge.label.length ->
                        existingEdge.target.addEdge(
                            label = label,
                            value = value,
                            start = start + existingEdge.label.length,
                            end = end,
                        )

                    // else the edge shares a prefix with part of the label -> the edge will be
                    // forked with an intermediate node containing the new value
                    else -> {
                        val intermediateNode = RadixTreeNodeImpl(
                            isTerminal = true,
                            value = value,
                            getKey = getKey,
                        )
                        val rootEdge = RadixTreeEdgeImpl(
                            label = existingEdge.label.substring(0, searchLabelLength),
                            target = intermediateNode
                        )
                        intermediateNode.addEdge(
                            label = existingEdge.label.substring(searchLabelLength),
                            target = existingEdge.target
                        )
                        edgeMap[firstChar] = rootEdge
                        rootEdge
                    }
                }

                // the split index was not -1, so an existing edge needs to be forked
            } else {
                val intermediateNode = RadixTreeNodeImpl<T>(
                    isTerminal = false,
                    value = null,
                    getKey = getKey,
                )
                val rootEdge = RadixTreeEdgeImpl(
                    label = existingEdge.label.substring(0, splitIndex),
                    target = intermediateNode
                )
                intermediateNode.addEdge(
                    label = existingEdge.label.substring(splitIndex),
                    target = existingEdge.target
                )
                intermediateNode.addEdge(
                    label = label.substring(start + splitIndex, end),
                    target = RadixTreeNodeImpl(
                        isTerminal = true,
                        value = value,
                        getKey = getKey,
                    )
                )
                edgeMap[firstChar] = rootEdge
                rootEdge
            }
        }
    }

    override fun removeEdge(
        label: String,
        start: Int,
        end: Int,
        parentEdge: RadixTreeEdgeImpl<T>?
    ): RadixTreeEdgeImpl<T>? = edges?.run {
        get(label[start])?.let{ edge ->
            val searchLabelLength = end - start
            if(searchLabelLength < label.length) return@let null
            for(i in 1 until label.length) {
                if(label[start + i] != edge.label[i]) return@let null
            }
            if(searchLabelLength == label.length) {
                if(edge.target.isTerminal){
                    if(edge.target.edgeCount > 1){
                        edge.apply{ target.isTerminal = false }
                    } else{
                        remove(label[start])?.also {
                            parentEdge?.apply{ this.label += edge.target.edges!!.toList().first().second }
                            if(isEmpty()) edges = null
                        }
                    }
                } else null
            } else {
                edge.target.removeEdge(
                    parentEdge = edge,
                    label = label,
                    start = label.length,
                    end = end
                )
            }
        }
    }

    override fun print(
        depth: Int,
        terminalChar: Char
    ){
        val prefix = "   ".repeat(depth)
        edges?.forEach{ (_, edge) ->
            val label = if(edge.target.isTerminal) "${edge.label}$terminalChar" else edge.label
            println("$prefix- $label")
            edge.target.print(depth + 1)
        }
    }
}
