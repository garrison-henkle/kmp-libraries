package dev.henkle.store

import android.content.Context
import dev.henkle.context.ContextProvider
import dev.henkle.store.provider.EncryptedSharedPreferencesStorage
import dev.henkle.store.provider.SharedPreferencesStorage

actual fun getPlatformStorageProviders(): PlatformStorageProviders =
    PlatformStorageProviders(
        context = ContextProvider.context
            ?: throw Exception("Unable to get Context from ContextProvider"),
    )

@Suppress("MemberVisibilityCanBePrivate")
actual class PlatformStorageProviders(context: Context) {
    val sharedPreferencesStorage = SharedPreferencesStorage(context = context)
    val encryptedSharedPreferencesStorage = EncryptedSharedPreferencesStorage(context = context)

    actual fun getPlatformDefaultStorage(): Storage = sharedPreferencesStorage
    actual fun getPlatformSecureStorage(): Storage = encryptedSharedPreferencesStorage
}
