package dev.henkle.store.provider

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import dev.henkle.store.Storage

/**
 * A [Storage] implementation backed by [SharedPreferences]
 */
@SuppressLint("ApplySharedPref")
class SharedPreferencesStorage(context: Context): AbstractSharedPreferencesStorage(
    preferences = context.getSharedPreferences(FILENAME, Context.MODE_PRIVATE),
) {
    companion object {
        private const val FILENAME = "storeKMP.store"
    }
}
