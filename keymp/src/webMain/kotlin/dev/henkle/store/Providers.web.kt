package dev.henkle.store

import dev.henkle.store.provider.CookieStorage
import dev.henkle.store.provider.CookieStorageImpl
import dev.henkle.store.provider.LocalStorage

actual fun getPlatformStorageProviders(): PlatformStorageProviders = PlatformStorageProviders()

actual class PlatformStorageProviders() {
    fun cookieStorage(cookieLifetimeMin: Long = DEFAULT_COOKIE_LIFETIME_MIN): CookieStorage =
        CookieStorageImpl(cookieLifetimeMin = cookieLifetimeMin)

    val localStorage = LocalStorage()
    val defaultCookieStorage: CookieStorage = cookieStorage()

    actual fun getPlatformDefaultStorage(): Storage = localStorage
    actual fun getPlatformSecureStorage(): Storage = defaultCookieStorage

    companion object {
        private const val DEFAULT_COOKIE_LIFETIME_MIN = 525_960L // 1 year
    }
}
