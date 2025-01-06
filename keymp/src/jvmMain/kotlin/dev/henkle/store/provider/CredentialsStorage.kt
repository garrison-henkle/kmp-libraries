package dev.henkle.store.provider

import com.microsoft.credentialstorage.StorageProvider
import com.microsoft.credentialstorage.model.StoredToken
import com.microsoft.credentialstorage.model.StoredTokenType
import dev.henkle.store.Storage

class CredentialsStorage : Storage {
    private val secureStorage = StorageProvider.getTokenStorage(
        true,
        StorageProvider.SecureOption.REQUIRED,
    )

    override fun get(key: String): String? = secureStorage[key]?.value?.concatToString()

    fun set(key: String, value: String, type: StoredTokenType) {
        val secret = StoredToken(value.toCharArray(), type)
        secureStorage.add(key, secret)
    }

    override fun set(key: String, value: String) =
        set(key = key, value = value, type = StoredTokenType.UNKNOWN)

    override fun clear(key: String) {
        secureStorage.delete(key)
    }
}
