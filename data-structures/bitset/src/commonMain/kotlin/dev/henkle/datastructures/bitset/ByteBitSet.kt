package dev.henkle.datastructures.bitset

/**
 * A bitset that can store a byte's worth of bits (8)
 */
interface ByteBitSet : BitSet {
    /**
     * The byte value of this bitset
     */
    val value: UByte
}
