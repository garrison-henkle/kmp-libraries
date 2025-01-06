package dev.henkle.store

import dev.henkle.store.provider.CookieStorage

actual fun getPlatformStorageProviders(): PlatformStorageProviders = PlatformStorageProviders()

@Suppress("MemberVisibilityCanBePrivate")
actual class PlatformStorageProviders() {
    fun cookieStorage(cookieLifetimeMin: Long = DEFAULT_COOKIE_LIFETIME_MIN) =
        getCookieStorage(cookieLifetimeMin = cookieLifetimeMin)

    val localStorage = getLocalStorage()
    val defaultCookieStorage = cookieStorage()

    actual fun getPlatformDefaultStorage(): Storage = localStorage
    actual fun getPlatformSecureStorage(): Storage = defaultCookieStorage

    companion object {
        private const val DEFAULT_COOKIE_LIFETIME_MIN = 525_960L // 1 year
    }
}

internal expect fun getCookieStorage(cookieLifetimeMin: Long): CookieStorage
internal expect fun getLocalStorage(): Storage
