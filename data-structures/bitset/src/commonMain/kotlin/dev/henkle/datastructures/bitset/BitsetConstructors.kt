package dev.henkle.datastructures.bitset

/**
 * Creates a bitset of size [size].
 *
 * All values are initialized to `false`
 */
fun BitSet(size: Int): BitSet = BitSetImpl(size = size)

/**
 * Creates a bitset that can store 8 bits
 *
 * All values are initialized to `false`
 */
fun ByteBitSet(): ByteBitSet = ByteBitSetImpl()
