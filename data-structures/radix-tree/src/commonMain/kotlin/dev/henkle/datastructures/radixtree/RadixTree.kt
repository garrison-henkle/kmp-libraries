package dev.henkle.datastructures.radixtree

/**
 * A radix tree that stores elements of type [T] using string keys.
 *
 * A string-based radix tree stores strings in
 *
 * See the [radix tree Wikipedia page](https://en.wikipedia.org/wiki/Radix_tree)
 * for a full explanation of the data structure.
 */
interface RadixTree<T: Any, N: RadixTreeNode<T, N, E>, E: RadixTreeEdge<T, E, N>> {
    /**
     * The root node of the tree
     */
    val root: N

    /**
     * Adds an element to the tree
     *
     * @param element the element to add
     */
    fun add(element: T)

    /**
     * Removes an element from the tree
     *
     * @param element the element to remove
     *
     * @return true if an element was removed from the tree, false otherwise
     */
    fun remove(element: T): Boolean

    /**
     * Determines if the provided element exists in the tree
     *
     * @param element the element to search for
     *
     * @return true if the element exists in the tree, false otherwise
     */
    fun exists(element: T): Boolean

    /**
     * Determines if the provided key exists in the tree.
     *
     * A key is considered existent if an exact match can be found
     * in the tree.
     *
     * Alias for [get] with exact set to true.
     *
     * @param key the key to search for
     *
     * @return true if the key exists in the tree, false otherwise
     */
    fun exists(key: String): Boolean

    /**
     * Determines if the provided prefix exists in the tree's keys.
     *
     * A prefix is considered existent if either an exact or partial
     * match can be found in the tree. If the prefix contains any
     * characters that do not match with any of the tree's keys,
     * it will not be considered a match, partial or otherwise.
     *
     * Alias for [get] with exact set to false.
     *
     * @param prefix the prefix to search for
     *
     * @return true if the prefix exists in the tree's keys, false otherwise
     */
    fun prefixExists(prefix: String): Boolean

    /**
     * Retrieves an element from the tree, if it exists.
     *
     * @param element the element to search for
     * @param exact when true, the element's key must
     * exactly match an existing key in the tree. When
     * false, a partial match of a key will suffice as
     * long as the prefix is truly a prefix of the found
     * key.
     *
     * @return the matching element or null if one was not found
     */
    fun get(element: T, exact: Boolean = false): T?

    /**
     * Retrieves an element from the tree by its key, if it exists.
     *
     * @param key the key to search for
     * @param exact when true, the key must exactly match
     * an existing key in the tree. When false, a partial
     * match of a key will suffice as long as the prefix
     * is truly a prefix of the found key.
     *
     * @return the element with a matching key or null if one was not found
     */
    fun get(key: String, exact: Boolean = false): T?

    /**
     * Prints a text representation of this tree to stdout
     */
    fun print()

    /**
     * Adds all of the provided elements to this tree
     *
     * @param elements the elements to add
     */
    fun addAll(elements: Iterable<T>) {
        elements.forEach(action = ::add)
    }

    /**
     * Adds all of the provided elements to this tree
     *
     * @param elements the elements to add
     */
    fun addAll(vararg elements: T) {
        elements.forEach(action = ::add)
    }

    /**
     * Removes all of the provided elements from this tree
     *
     * @param elements the elements to remove
     *
     * @return true if any elements were removed from the tree,
     * false otherwise
     */
    fun removeAll(elements: Iterable<T>): Boolean =
        elements.map(transform = ::remove).any { it }

    /**
     * Removes all of the provided elements from this tree
     *
     * @param elements the elements to remove
     *
     * @return true if any elements were removed from the tree,
     * false otherwise
     */
    fun removeAll(vararg elements: T): Boolean =
        elements.map(transform = ::remove).any { it }

    /**
     * Retrieves an element from the tree by its key, if it exists.
     *
     * This method for partial key matches as long as the provided
     * key is a prefix of the found key.
     *
     * @return the element with a matching key or null if one was not found
     */
    operator fun get(key: String): T? = get(key = key, exact = false)

    /**
     * Adds the provided element to this tree
     *
     * @param element the element to add
     */
    operator fun plus(element: T) = add(element = element)

    /**
     * Removes the provided element from this tree
     *
     * @param element the element to remove
     *
     * @return true if an element was removed from the tree,
     * false otherwise
     */
    operator fun minus(element: T) = remove(element = element)
}
