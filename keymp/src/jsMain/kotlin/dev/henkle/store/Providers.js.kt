package dev.henkle.store

import dev.henkle.store.provider.CookieStorage
import dev.henkle.store.provider.CookieStorageImpl
import dev.henkle.store.provider.LocalStorage

internal actual fun getCookieStorage(cookieLifetimeMin: Long): CookieStorage =
    CookieStorageImpl(cookieLifetimeMin = cookieLifetimeMin)

internal actual fun getLocalStorage(): Storage = LocalStorage()
