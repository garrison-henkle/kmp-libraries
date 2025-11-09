package dev.henkle.datastructures.stack

/**
 * Common Kotlin immutable stack implementation that generally follows
 * the API of Java's `java.util.Stack`.
 *
 * @see MutableStack
 */
interface Stack<T: Any> : Collection<T> {
    /**
     * The number of elements contained in this stack
     */
    override val size: Int

    /**
     * View the topmost element without removing it
     *
     * @return the topmost element or null if the stack is empty
     */
    fun peek(): T?

    /**
     * Returns the 1-based position of where an object is on this [Stack].
     *
     * If [element] exists, this method returns the distance to the
     * occurrence nearest to the top of the [Stack], where the topmost
     * element is considered to be at distance 1.
     *
     * @param element the element ot find in the [Stack]
     *
     * @return the 1-based position of the first matching element or -1
     * if no matching element was found
     */
    fun search(element: T): Int

    /**
     * Tests if this [Stack] is empty
     *
     * @return true if the stack is empty, false otherwise
     */
    override fun isEmpty(): Boolean

    /**
     * Tests if this [Stack] contains elements
     *
     * @return true if the stack contains elements, false otherwise
     */
    fun isNotEmpty(): Boolean

    /**
     * Returns a [MutableStack] containing the elements of this [Stack]
     *
     * @return a mutable shallow copy of this [Stack]
     */
    fun toMutable(): MutableStack<T>

    /**
     * Returns a [MutableStack] containing the elements of this [Stack]
     *
     * @return if the underlying implementation of this [Stack] is already
     * a [MutableStack], then this [Stack] casted as [MutableStack]. Otherwise
     * a mutable shallow copy of this [Stack] will be returned.
     */
    fun asMutableOrCreate(): MutableStack<T>
}