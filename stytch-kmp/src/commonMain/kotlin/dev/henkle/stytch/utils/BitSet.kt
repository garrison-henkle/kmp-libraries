/**
 * Copyright (c) 2017-2019 Carlos Ballesteros Velasco and contributors
 * * https://github.com/korlibs/korge/graphs/contributors
 * * https://github.com/korlibs-archive/
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.henkle.stytch.utils

/**
 * Fixed size [BitSet]. Similar to a [BooleanArray] but tightly packed to reduce memory usage.
 *
 * Taken from Korge's korlibs-datastructures:
 * https://github.com/korlibs/korge-korlibs/blob/de91b02a15ccea72371f4e5a70ecd72303264ad1/korlibs-datastructure/src/korlibs/datastructure/BitSet.kt
 */
class BitSet(override val size: Int) : Collection<Boolean> {
    private infix fun Int.divCeil(that: Int): Int =
        if (this % that != 0) (this / that) + 1 else (this / that)

    private val data = IntArray(size divCeil 32)

    private fun part(index: Int) = index ushr 5
    private fun bit(index: Int) = index and 0x1f

    operator fun get(index: Int): Boolean = ((data[part(index)] ushr (bit(index))) and 1) != 0
    operator fun set(index: Int, value: Boolean) {
        val i = part(index)
        val b = bit(index)
        if (value) {
            data[i] = data[i] or (1 shl b)
        } else {
            data[i] = data[i] and (1 shl b).inv()
        }
    }

    fun set(index: Int): Unit = set(index, true)
    fun unset(index: Int): Unit = set(index, false)

    fun clear(): Unit = data.fill(0)

    override fun contains(element: Boolean): Boolean = indices.any { this[it] == element }
    override fun containsAll(elements: Collection<Boolean>): Boolean = when {
        elements.contains(true) && !this.contains(true) -> false
        elements.contains(false) && !this.contains(false) -> false
        else -> true
    }

    override fun isEmpty(): Boolean = isEmpty()
    override fun iterator(): Iterator<Boolean> = indices.map { this[it] }.iterator()

    override fun hashCode(): Int = data.contentHashCode() + size
    override fun equals(other: Any?): Boolean = (other is BitSet) && this.size == other.size && this.data.contentEquals(other.data)
}