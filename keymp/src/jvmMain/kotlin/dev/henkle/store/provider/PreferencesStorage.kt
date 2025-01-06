package dev.henkle.store.provider

import dev.henkle.store.Storage
import java.util.prefs.Preferences

class PreferencesStorage : Storage {
    private val preferences = Preferences.userRoot()
    override fun get(key: String): String? = preferences.get(key, null)
    override fun set(key: String, value: String) = preferences.put(key, value)
    override fun clear(key: String) = preferences.remove(key)

    override fun getString(key: String, default: String): String = preferences.get(key, default)
    override fun getBoolean(key: String, default: Boolean): Boolean = preferences.getBoolean(key, default)
    override fun getInt(key: String, default: Int): Int = preferences.getInt(key, default)
    override fun getLong(key: String, default: Long): Long = preferences.getLong(key, default)
    override fun getFloat(key: String, default: Float): Float = preferences.getFloat(key, default)
    override fun getDouble(key: String, default: Double): Double = preferences.getDouble(key, default)

    override fun set(key: String, value: Boolean) = preferences.putBoolean(key, value)
    override fun set(key: String, value: Int) = preferences.putInt(key, value)
    override fun set(key: String, value: Long) = preferences.putLong(key, value)
    override fun set(key: String, value: Float) = preferences.putFloat(key, value)
    override fun set(key: String, value: Double) = preferences.putDouble(key, value)
}
