package dev.henkle.datastructures.queue

/**
 * Common Kotlin immutable queue implementation that generally
 * follows the API of Java's `java.util.Queue`.
 *
 * @see Queue
 */
interface MutableQueue<T: Any> : Queue<T> {
    /**
     * Places the provided element at the back of the queue
     *
     * @return true to mimic `java.util.Queue`'s (and
     * therefore `java.util.Collection`'s) `add(T)`
     */
    fun add(element: T): Boolean

    /**
     * Removes and returns the element at the front of this queue
     *
     * @return the removed element or null if the queue is empty
     */
    fun poll(): T?
}
