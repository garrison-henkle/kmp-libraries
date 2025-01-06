package dev.henkle.store.provider

import android.annotation.SuppressLint
import android.content.SharedPreferences
import dev.henkle.store.Storage

@SuppressLint("ApplySharedPref")
abstract class AbstractSharedPreferencesStorage(
    private val preferences: SharedPreferences,
) : Storage {
    private val editor: SharedPreferences.Editor get() = preferences.edit()

    override fun get(key: String): String? =
        preferences.getString(key, null)

    override fun set(key: String, value: String) {
        editor.putString(key, value).commit()
    }

    override fun clear(key: String) {
        editor.remove(key).commit()
    }

    override fun getString(key: String, default: String): String = preferences.getString(key, default)!!
    override fun getBoolean(key: String, default: Boolean): Boolean = preferences.getBoolean(key, default)
    override fun getInt(key: String, default: Int): Int = preferences.getInt(key, default)
    override fun getLong(key: String, default: Long): Long = preferences.getLong(key, default)
    override fun getFloat(key: String, default: Float): Float = preferences.getFloat(key, default)
    override fun getDouble(key: String, default: Double): Double =
        preferences.getFloat(key, default.toFloat()).toDouble()

    override fun set(key: String, value: Boolean) =
        set(key = key, value = value, setter = editor::putBoolean)

    override fun set(key: String, value: Int) =
        set(key = key, value = value, setter = editor::putInt)

    override fun set(key: String, value: Long) =
        set(key = key, value = value, setter = editor::putLong)

    override fun set(key: String, value: Float) =
        set(key = key, value = value, setter = editor::putFloat)

    override fun set(key: String, value: Double) =
        set(key = key, value = value) { _, _ -> editor.putFloat(key, value.toFloat()) }

    private fun <T: Any> set(
        key: String,
        value: T,
        setter: (key: String, value: T) -> SharedPreferences.Editor,
    ) {
        setter(key, value).commit()
    }
}
