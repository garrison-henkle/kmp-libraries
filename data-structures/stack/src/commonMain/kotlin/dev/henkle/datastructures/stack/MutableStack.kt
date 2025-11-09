package dev.henkle.datastructures.stack

/**
 * Common Kotlin mutable stack implementation that generally follows
 * the API of Java's `java.util.Stack`.
 *
 * @see Stack
 */
interface MutableStack<T: Any> : Stack<T> {
    /**
     * Pushes the provided [element] to the top of the stack
     *
     * @param element the element to add to the top of the stack
     *
     * @return the provided [element]
     */
    fun push(element: T): T

    /**
     * Removes and returns the topmost element of this stack
     *
     * @return the removed element or null if the stack is empty
     */
    fun pop(): T?
}
