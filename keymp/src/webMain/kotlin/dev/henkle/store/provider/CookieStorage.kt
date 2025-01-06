package dev.henkle.store.provider

import dev.henkle.store.Storage
import dev.henkle.store.model.cookie.Cookie

interface CookieStorage : Storage {
    fun set(cookie: Cookie)
}
