/*
Copyright (c) 2017-2019 Carlos Ballesteros Velasco and contributors
https://github.com/korlibs/korge/graphs/contributors
https://github.com/korlibs-archive/

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package dev.henkle.datastructures.bitset

import kotlin.math.absoluteValue
import kotlin.math.sign

/**
 * Fixed size [BitSet]. Similar to a [BooleanArray] but tightly packed to reduce memory usage.
 *
 * Taken from
 * https://github.com/korlibs/korlibs-datastructure/blob/87d263055e42d32d11f7a7eade09b17d789a253c/korlibs-datastructure/src/korlibs/datastructure/BitSet.kt
 */
internal class BitSetImpl(override val size: Int) : BitSet {
    override val indices: IntRange = 0..<size
    private val data = IntArray(size ceilDiv 32)

    private fun part(index: Int) = index ushr 5
    private fun bit(index: Int) = index and 0x1f

    override operator fun get(index: Int): Boolean =
        ((data[part(index)] ushr (bit(index))) and 1) != 0

    override operator fun set(index: Int, value: Boolean) {
        val i = part(index)
        val b = bit(index)
        if (value) {
            data[i] = data[i] or (1 shl b)
        } else {
            data[i] = data[i] and (1 shl b).inv()
        }
    }

    override fun clear(): Unit = data.fill(0)

    override fun contains(element: Boolean): Boolean = indices.any { this[it] == element }
    override fun containsAll(elements: Collection<Boolean>): Boolean = when {
        elements.contains(true) && !this.contains(true) -> false
        elements.contains(false) && !this.contains(false) -> false
        else -> true
    }

    override fun isEmpty(): Boolean = size == 0
    override fun isNotEmpty(): Boolean = size != 0

    override fun iterator(): Iterator<Boolean> = indices.map { this[it] }.iterator()

    override fun hashCode(): Int = data.contentHashCode() + size
    override fun equals(other: Any?): Boolean =
        other is BitSetImpl &&
            this.size == other.size &&
            this.data.contentEquals(other.data)

    private infix fun Int.ceilDiv(other: Int): Int =
        this.floorDiv(other) + (this % other).sign.absoluteValue
}
