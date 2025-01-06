package dev.henkle.store.provider

import android.annotation.SuppressLint
import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

@SuppressLint("ApplySharedPref")
class EncryptedSharedPreferencesStorage(context: Context): AbstractSharedPreferencesStorage(
    preferences = run {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        EncryptedSharedPreferences.create(
            FILENAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    },
) {
    companion object {
        private const val FILENAME = "storeKMPSecure.store"
    }
}
