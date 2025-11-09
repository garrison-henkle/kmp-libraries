package dev.henkle.store

import dev.henkle.store.provider.CookieStorage
import dev.henkle.store.provider.LocalStorage

actual fun getPlatformStorageProviders(): PlatformStorageProviders = PlatformStorageProviders()

actual class PlatformStorageProviders() {
    fun cookieStorage(cookieLifetimeMin: Long = DEFAULT_COOKIE_LIFETIME_MIN): CookieStorage =
        CookieStorage(cookieLifetimeMin = cookieLifetimeMin)

    val localStorage = LocalStorage()

    actual fun getPlatformDefaultStorage(): Storage = localStorage
    actual fun getPlatformSecureStorage(): Storage = cookieStorage()

    companion object {
        private const val DEFAULT_COOKIE_LIFETIME_MIN = 525_960L // 1 year
    }
}
