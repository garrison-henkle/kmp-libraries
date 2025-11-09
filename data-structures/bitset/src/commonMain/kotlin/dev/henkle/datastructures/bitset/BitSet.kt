package dev.henkle.datastructures.bitset

/**
 * A variable length bitset. Allows for tightly packing boolean
 * values into a smaller memory footprint.
 */
interface BitSet : Collection<Boolean> {
    /**
     * The valid indices that can be get or set in this bitset
     */
    val indices: IntRange

    /**
     * Gets the boolean value stored at the provided index
     *
     * @param index the index to read
     *
     * @return the value stored at the provided index
     */
    operator fun get(index: Int): Boolean

    /**
     * Sets the boolean value stored at the provided index
     *
     * @param index the index to change
     * @param value the value to set
     */
    operator fun set(index: Int, value: Boolean)

    /**
     * Clears all of the values set in this bitset
     */
    fun clear()

    /**
     * Determines if this bitset contains items
     */
    fun isNotEmpty(): Boolean

    /**
     * Sets the provided index to true
     *
     * @param index the index to set
     */
    fun set(index: Int): Unit = set(index = index, value = true)

    /**
     * Sets the provided index to false
     *
     * @param index the index to set
     */
    fun unset(index: Int): Unit = set(index = index, value = false)
}
