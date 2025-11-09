package dev.henkle.encoding.image


data class IconSize(val width: Int, val height: Int) : Comparable<IconSize> {
    override fun compareTo(other: IconSize): Int = when{
        this.width == other.width -> 0
        this.width == unknown.width -> Int.MAX_VALUE
        other.width == unknown.width -> Int.MIN_VALUE
        this.width == any.width -> Int.MIN_VALUE
        other.width == any.width -> Int.MAX_VALUE
        else -> this.width - other.width
    }

    companion object{
        val any = IconSize(-1, -1)
        val unknown = IconSize(-2, -2)
    }
}
