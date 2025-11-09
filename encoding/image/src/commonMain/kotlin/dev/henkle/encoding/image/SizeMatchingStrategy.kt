package dev.henkle.encoding.image


enum class SizeMatchingStrategy{
    /**
     * Finds the largest item in the collection
     */
    Largest,

    /**
     * Finds the exact match or, if no exact match exists, the next largest item.
     *
     * If no items exist that are larger than or equal to the exact
     * match, then the largest item is returned
     */
    ExactOrNextLargest,

    /**
     * Finds the exact match or, if no exact match exists, the next smallest item.
     *
     * If no items exist that are smaller than or equal to the exact
     * match, then the smallest item is returned
     */
    ExactOrNextSmallest,

    /**
     * Finds the smallest item in the collection
     */
    Smallest;

    val allowsExact: Boolean get() = this == ExactOrNextLargest || this == ExactOrNextSmallest
}
