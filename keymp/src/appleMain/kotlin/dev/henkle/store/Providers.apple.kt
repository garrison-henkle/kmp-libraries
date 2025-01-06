package dev.henkle.store

import dev.henkle.store.keychain.KeychainClient
import dev.henkle.store.keychain.KeychainClientImpl
import dev.henkle.store.provider.KeychainStorage
import dev.henkle.store.provider.UserDefaultsStorage

actual fun getPlatformStorageProviders(): PlatformStorageProviders = PlatformStorageProviders()

@Suppress("MemberVisibilityCanBePrivate")
actual class PlatformStorageProviders() {
    private val keychainClient: KeychainClient = KeychainClientImpl()

    val userDefaultsStorage = UserDefaultsStorage()
    val keychainStorage = KeychainStorage(keychain = keychainClient)

    actual fun getPlatformDefaultStorage(): Storage = userDefaultsStorage
    actual fun getPlatformSecureStorage(): Storage = keychainStorage
}
