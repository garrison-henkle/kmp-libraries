package dev.henkle.datastructures.stack

import com.github.michaelbull.result.Result
import dev.henkle.datastructures.stack.Stack.EmptyStackException

/**
 * Common Kotlin mutable stack implementation that generally follows
 * the API of Java's java.util.Stack. The only major deviation is that
 * this [MutableStack] implementation uses [Result] types instead of
 * nullables to allow for null values to be valid elements.
 *
 * @see Stack
 */
interface MutableStack<T> : Stack<T> {
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
     * @return the removed element or an Err with an
     * EmptyStackException if the stack is empty
     */
    fun pop(): Result<T, EmptyStackException>
}
