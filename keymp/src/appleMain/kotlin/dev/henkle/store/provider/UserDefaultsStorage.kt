package dev.henkle.store.provider

import dev.henkle.store.Storage
import platform.Foundation.NSString
import platform.Foundation.NSUserDefaults

@Suppress("CAST_NEVER_SUCCEEDS")
class UserDefaultsStorage : Storage {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun get(key: String): String? = defaults.stringForKey(defaultName = key)

    override fun set(key: String, value: String) =
        defaults.setObject(value = value as NSString, forKey = key)

    override fun clear(key: String) = defaults.removeObjectForKey(defaultName = key)

    override fun getString(key: String, default: String): String = get(key = key) ?: default

    override fun getBoolean(key: String, default: Boolean): Boolean =
        getOrDefault(key = key, default = default, read = defaults::boolForKey)

    override fun getInt(key: String, default: Int): Int =
        getOrDefault(key = key, default = default) { defaults.integerForKey(defaultName = key).toInt() }

    override fun getLong(key: String, default: Long): Long =
        getOrDefault(key = key, default = default, read = defaults::integerForKey)

    override fun getFloat(key: String, default: Float): Float =
        getOrDefault(key = key, default = default, read = defaults::floatForKey)

    override fun getDouble(key: String, default: Double): Double =
        getOrDefault(key = key, default = default, read = defaults::doubleForKey)

    override fun set(key: String, value: Boolean) = defaults.setBool(value = value, forKey = key)
    override fun set(key: String, value: Int) = defaults.setInteger(value = value.toLong(), forKey = key)
    override fun set(key: String, value: Long) = defaults.setInteger(value = value, forKey = key)
    override fun set(key: String, value: Float) = defaults.setFloat(value = value, forKey = key)
    override fun set(key: String, value: Double) = defaults.setDouble(value = value, forKey = key)

    private fun exists(key: String): Boolean = defaults.objectForKey(defaultName = key) != null
    private fun <T: Any> getOrDefault(key: String, default: T, read: (key: String) -> T): T =
        if (exists(key = key)) read(key) else default
}
