package dev.henkle.store

import dev.henkle.store.provider.CredentialsStorage
import dev.henkle.store.provider.PreferencesStorage

actual fun getPlatformStorageProviders(): PlatformStorageProviders = PlatformStorageProviders()

@Suppress("MemberVisibilityCanBePrivate")
actual class PlatformStorageProviders {
    val preferencesStorage = PreferencesStorage()
    val credentialsStorage = CredentialsStorage()

    actual fun getPlatformDefaultStorage(): Storage = preferencesStorage
    actual fun getPlatformSecureStorage(): Storage = credentialsStorage
}