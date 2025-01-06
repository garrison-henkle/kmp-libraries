package dev.henkle.korvus.internal.utils

class TripleIterator<A, B, C>(
    first: Iterable<A>,
    second: Iterable<B>,
    third: Iterable<C>,
) : Iterator<Triple<A, B, C>> {
    private val first = first.iterator()
    private val second = second.iterator()
    private val third = third.iterator()
    override fun hasNext(): Boolean = first.hasNext() && second.hasNext() && third.hasNext()
    override fun next(): Triple<A, B, C> = Triple(first.next(), second.next(), third.next())
}

class QuadrupleIterator<A, B, C>(
    first: Iterable<A>,
    second: Iterable<B>,
    third: Iterable<C>,
) : Iterator<Triple<A, B, C>> {
    private val first = first.iterator()
    private val second = second.iterator()
    private val third = third.iterator()

    override fun hasNext(): Boolean = first.hasNext() && second.hasNext() && third.hasNext()

    override fun next(): Triple<A, B, C> = Triple(first.next(), second.next(), third.next())
}

internal data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
)
