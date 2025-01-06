package dev.henkle.store.provider

import dev.henkle.store.Storage
import dev.henkle.store.keychain.KeychainClient
import dev.henkle.store.keychain.KeychainItem

class KeychainStorage(private val keychain: KeychainClient): Storage {
    override fun get(key: String): String? = keychain.getString(item = KeychainItem(name = key))

    override fun set(key: String, value: String) =
        keychain.setString(item = KeychainItem(name = key), value = value)

    override fun clear(key: String) {
        keychain.removeItem(item = KeychainItem(name = key))
    }
}
