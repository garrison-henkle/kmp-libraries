package dev.henkle.datastructures.radixtree

/**
 * Instantiates a radix tree that can store elements of type [T]
 *
 * @param getKey lambda for retrieving a [T] element's unique identifier. Behavior of the tree is
 * undefined if this lambda can return the same identifier for two distinct elements
 */
fun <T: Any> RadixTree(
    getKey: (T) -> String,
): RadixTree<T, *, *> =
    RadixTreeImpl(getKey = getKey)

/**
 * Instantiates a radix tree that can store elements of type [T]
 *
 * @param elements the elements to initialize the tree with
 * @param getKey lambda for retrieving a [T] element's unique identifier. Behavior of the tree is
 * undefined if this lambda can return the same identifier for two distinct elements
 */
fun <T: Any> RadixTree(
    elements: Iterable<T>,
    getKey: (T) -> String,
): RadixTree<T, *, *> =
    RadixTreeImpl(getKey = getKey)
        .apply { elements.forEach(::add) }

/**
 * Instantiates a radix tree that can store Strings
 */
fun RadixTree(): RadixTree<String, *, *> =
    RadixTreeImpl { it }

/**
 * Instantiates a radix tree that can store Strings
 *
 * @param elements the elements to initialize the tree with
 */
fun RadixTree(elements: Iterable<String>): RadixTree<String, *, *> =
    RadixTreeImpl<String> { it }
        .apply { elements.forEach(::add) }

internal inline fun <K, V, M: Map<K, V>> M?.getOrCreate(
    create: () -> M,
    assign: (M) -> Unit,
): M = this ?: run{
    create().also{ assign(it) }
}

internal inline fun <K, V> HashMap<K, V>?.getOrCreate(
    assign: (HashMap<K, V>) -> Unit,
): HashMap<K, V> =
    getOrCreate(
        create = ::HashMap,
        assign = assign,
    )
