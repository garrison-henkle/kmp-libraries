package dev.henkle.datastructures.stack

internal class StackImpl<T: Any>(
    private val list: MutableList<T> = mutableListOf(),
) : MutableStack<T> {
    override val size: Int get() = list.size

    override fun push(element: T): T {
        list += element
        return element
    }

    override fun pop(): T? = list.removeLastOrNull()

    override fun peek(): T? = list.lastOrNull()

    override fun search(element: T): Int {
        val index = list.indexOf(element = element)
        return if (index != -1) {
            list.size - index
        } else {
            -1
        }
    }

    override fun isEmpty(): Boolean = list.isEmpty()

    override fun isNotEmpty(): Boolean = list.isNotEmpty()

    override fun toMutable(): MutableStack<T> =
        StackImpl(list = list.toMutableList())

    override fun asMutableOrCreate(): MutableStack<T> = this

    // Collection methods

    override fun contains(element: T): Boolean = element in list

    override fun containsAll(elements: Collection<T>): Boolean =
        list.containsAll(elements = elements)

    override fun iterator(): Iterator<T> =
        StackIterator(backingList = list)

    private class StackIterator<T>(
        private val backingList: List<T>,
    ) : Iterator<T> {
        private var index = backingList.lastIndex

        override fun next(): T =
            backingList[index--]

        override fun hasNext(): Boolean =
            index in backingList.indices
    }
}
