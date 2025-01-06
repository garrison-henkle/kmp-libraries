package dev.henkle.store.provider

import android.annotation.SuppressLint
import android.content.Context

@SuppressLint("ApplySharedPref")
class SharedPreferencesStorage(context: Context): AbstractSharedPreferencesStorage(
    preferences = context.getSharedPreferences(FILENAME, Context.MODE_PRIVATE),
) {
    companion object {
        private const val FILENAME = "storeKMP.store"
    }
}
