package dev.henkle.datastructures.queue

internal class QueueImpl<T: Any>(
    private val list: MutableList<T> = mutableListOf(),
) : MutableQueue<T> {
    override val size: Int get() = list.size

    override fun add(element: T) = list.add(element = element)

    override fun poll(): T? = list.removeFirstOrNull()

    override fun peek(): T? = list.firstOrNull()

    override fun isEmpty(): Boolean = list.isEmpty()

    override fun isNotEmpty(): Boolean = list.isNotEmpty()

    override fun toMutable(): MutableQueue<T> =
        QueueImpl(list = list.toMutableList())

    override fun asMutableOrCreate(): MutableQueue<T> = this

    // Collection methods

    override fun contains(element: T): Boolean =
        element in list

    override fun containsAll(elements: Collection<T>): Boolean =
        list.containsAll(elements = elements)

    override fun iterator(): Iterator<T> = list.iterator()
}
