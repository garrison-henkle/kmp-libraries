package dev.henkle.datastructures.bitset

/**
 * [BitSet] of length 8. Mostly based on [BitSetImpl], just adapted
 * to only use 8 bits.
 */
internal class ByteBitSetImpl : ByteBitSet {
    override val size: Int = UByte.SIZE_BITS
    override val indices: IntRange = 0..<UByte.SIZE_BITS

    // Kotlin doesn't provide bit shifting operators on anything
    // smaller than an Int, so use a UInt as the backing field here
    private var data: UInt = 0u

    override val value: UByte = data.toUByte()

    override operator fun get(index: Int): Boolean {
        require(value = index in indices) {
            "Cannot get index $index because it is out of bounds!"
        }
        return (data shr index and 1u) != 0u
    }

    override operator fun set(index: Int, value: Boolean) {
        require(value = index in indices) {
            "Cannot set index $index because it is out of bounds!"
        }
        if (value) {
            this.data = this.data or (1u shl index)
        } else {
            this.data = this.data and (1u shl index).inv()
        }
    }

    override fun clear() {
        data = 0u
    }

    override fun contains(element: Boolean): Boolean =
        indices.any { this[it] == element }

    override fun containsAll(elements: Collection<Boolean>): Boolean =
        when {
            true in elements && true !in this -> false
            false in elements && false !in this -> false
            else -> true
        }

    override fun isEmpty(): Boolean = false
    override fun isNotEmpty(): Boolean = true
    override fun iterator(): Iterator<Boolean> = indices.map { this[it] }.iterator()
}
