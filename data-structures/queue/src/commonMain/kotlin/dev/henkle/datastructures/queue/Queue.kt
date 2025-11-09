package dev.henkle.datastructures.queue

/**
 * Common Kotlin immutable queue implementation that generally
 * follows the API of Java's `java.util.Queue`.
 *
 * @see MutableQueue
 */
interface Queue<T: Any> : Collection<T> {
    /**
     * The number of elements contained in this queue
     */
    override val size: Int

    /**
     * View the frontmost element in this queue without removing it
     *
     * @return the frontmost element or null if the queue is empty
     */
    fun peek(): T?

    /**
     * Tests if this [Queue] is empty
     *
     * @return true if the queue is empty, false otherwise
     */
    override fun isEmpty(): Boolean

    /**
     * Tests if this [Queue] contains elements
     *
     * @return true if the queue contains elements, false otherwise
     */
    fun isNotEmpty(): Boolean

    /**
     * Returns a [MutableQueue] containing the elements of this [Queue]
     *
     * @return a mutable shallow copy of this [Queue]
     */
    fun toMutable(): MutableQueue<T>

    /**
     * Returns a [MutableQueue] containing the elements of this [Queue]
     *
     * @return if the underlying implementation of this [Queue] is already
     * a [MutableQueue], then this [Queue] casted as [MutableQueue]. Otherwise
     * a mutable shallow copy of this [Queue] will be returned.
     */
    fun asMutableOrCreate(): MutableQueue<T>
}
