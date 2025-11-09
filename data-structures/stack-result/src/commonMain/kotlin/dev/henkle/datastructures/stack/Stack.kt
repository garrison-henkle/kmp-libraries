package dev.henkle.datastructures.stack

import com.github.michaelbull.result.Result

/**
 * Common Kotlin immutable stack implementation that generally follows
 * the API of Java's `java.util.Stack`. The only major deviation is that
 * this [Stack] implementation uses [Result] types instead of nullables
 * to allow for null values to be valid elements.
 *
 * @see MutableStack
 */
interface Stack<T> : Collection<T> {
    /**
     * The number of elements
     */
    override val size: Int

    /**
     * View the topmost element without removing it
     *
     * @return the topmost element or an Err with an
     * EmptyStackException if no such element exists
     */
    fun peek(): Result<T, EmptyStackException>

    /**
     * Returns the 1-based position of where an object is on this [Stack].
     *
     * If [element] exists, this method returns the distance to the
     * occurrence nearest to the top of the [Stack], where the topmost
     * element is considered to be at distance 1.
     *
     * @param element the element ot find in the [Stack]
     *
     * @return the 1-based position of the first matching element or an
     * Err with a NotFoundException if no matching element could be found
     */
    fun search(element: T): Result<Int, NotFoundException>

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

    /**
     * Exception indicating that the stack is currently empty
     */
    class EmptyStackException : Exception()

    /**
     * Exception indicating that the searched for element could not be found
     */
    class NotFoundException : Exception()
}