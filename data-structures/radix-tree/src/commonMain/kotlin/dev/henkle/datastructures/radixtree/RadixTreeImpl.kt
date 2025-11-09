package dev.henkle.datastructures.radixtree

import kotlin.math.min

/**
 * The default implementation of [RadixTree].
 */
internal class RadixTreeImpl<T: Any>(
    private val getKey: (T) -> String,
) : RadixTree<T, RadixTreeNodeImpl<T>, RadixTreeEdgeImpl<T>> {
    override val root = RadixTreeNodeImpl<T>(
        isTerminal = false,
        value = null,
        getKey = getKey,
    )

    override fun add(element: T) {
        root.addEdge(
            label = getKey(element),
            value = element,
        )
    }

    override fun remove(element: T): Boolean =
        root.removeEdge(label = getKey(element)) != null

    private fun exists(string: String, exact: Boolean): Boolean {
        var currentCharIndex = 0
        var currentNode: RadixTreeNodeImpl<T>? = root
        var currentEdge: RadixTreeEdgeImpl<T>? = null
        var currentLabel: String?
        while (currentCharIndex < string.length) {
            currentEdge = currentNode?.getEdge(string[currentCharIndex++])
            if (currentCharIndex >= string.length) break
            currentLabel = currentEdge?.label
            if (currentLabel != null) {
                for (i in 1 until min(currentLabel.length, string.length)) {
                    if(currentLabel[i] != string[currentCharIndex]) {
                        return !exact || currentEdge?.target?.isTerminal == true
                    }
                    currentCharIndex += 1
                }
            } else {
                break
            }
            currentNode = currentEdge?.target
        }
        return !exact || currentEdge?.target?.isTerminal == true
    }

    override fun exists(element: T): Boolean = exists(string = getKey(element), exact = true)
    override fun exists(key: String): Boolean = exists(string = key, exact = true)
    override fun prefixExists(prefix: String): Boolean = exists(string = prefix, exact = false)

    override fun print() = root.print()

    override fun get(element: T, exact: Boolean): T? =
        get(key = getKey(element), exact = exact)

    override fun get(key: String, exact: Boolean): T? {
        var currentCharIndex = 0
        var currentNode: RadixTreeNodeImpl<T>? = root
        var currentEdge: RadixTreeEdgeImpl<T>? = null
        var currentLabel: String?
        while (currentCharIndex < key.length) {
            if (currentCharIndex >= key.length) break
            currentEdge = currentNode?.getEdge(key[currentCharIndex++])
            currentLabel = currentEdge?.label
            if (currentLabel != null) {
                val minLength = min(currentLabel.length, key.length - currentCharIndex + 1)
                for (i in 1 ..<minLength) {
                    if (currentLabel[i] != key[currentCharIndex]) {
                        return currentEdge?.target?.takeIf { !exact && it.isTerminal }?.value
                    }
                    currentCharIndex += 1
                }
            } else {
                break
            }
            currentNode = currentEdge?.target
        }
        return currentEdge?.target
            ?.takeIf { !exact || it.isTerminal }
            ?.value
    }
}
