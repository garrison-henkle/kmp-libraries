package dev.henkle.store

interface Storage {
    operator fun get(key: String): String?
    operator fun set(key: String, value: String)
    fun clear(key: String)

    fun getString(key: String, default: String): String = get(key = key) ?: default
    fun getBoolean(key: String, default: Boolean): Boolean = get(key = key)?.toBooleanStrict() ?: default
    fun getInt(key: String, default: Int): Int = get(key = key)?.toInt() ?: default
    fun getLong(key: String, default: Long): Long = get(key = key)?.toLong() ?: default
    fun getFloat(key: String, default: Float): Float = get(key = key)?.toFloat() ?: default
    fun getDouble(key: String, default: Double): Double = get(key = key)?.toDouble() ?: default

    operator fun set(key: String, value: Boolean) = set(key = key, value = value.toString())
    operator fun set(key: String, value: Int) = set(key = key, value = value.toString())
    operator fun set(key: String, value: Long) = set(key = key, value = value.toString())
    operator fun set(key: String, value: Float) = set(key = key, value = value.toString())
    operator fun set(key: String, value: Double) = set(key = key, value = value.toString())
}
