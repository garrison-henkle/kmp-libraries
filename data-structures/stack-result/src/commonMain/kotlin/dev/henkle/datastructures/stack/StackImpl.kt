package dev.henkle.datastructures.stack

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import dev.henkle.datastructures.stack.Stack.EmptyStackException
import dev.henkle.datastructures.stack.Stack.NotFoundException

internal class StackImpl<T>(
    private val list: MutableList<T> = mutableListOf(),
) : MutableStack<T> {
    override val size: Int get() = list.size

    override fun push(element: T): T {
        list += element
        return element
    }

    override fun pop(): Result<T, EmptyStackException> =
        list.removeLastOrNull()?.let(::Ok)
            ?: Err(error = EmptyStackException())

    override fun peek(): Result<T, EmptyStackException> =
        list.lastOrNull()?.let(::Ok)
            ?: Err(error = EmptyStackException())

    override fun search(element: T): Result<Int, NotFoundException> {
        val index = list.indexOf(element = element)
        return if (index != -1) {
            Ok(value = list.size - index)
        } else {
            Err(error = NotFoundException())
        }
    }

    override fun isEmpty(): Boolean = list.isEmpty()

    override fun isNotEmpty(): Boolean = list.isNotEmpty()

    override fun toMutable(): MutableStack<T> =
        StackImpl(list = list.toMutableList())

    override fun asMutableOrCreate(): MutableStack<T> = this

    // Collection methods

    override fun contains(element: T): Boolean =
        search(element = element).isOk

    override fun containsAll(elements: Collection<T>): Boolean =
        elements.all { element -> search(element = element).isOk }

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
