package dev.henkle.store.provider

import dev.henkle.store.Storage
import kotlinx.browser.window

class LocalStorage : Storage {
    override fun get(key: String): String? =
        window.localStorage.getItem(key = key)

    override fun set(key: String, value: String) =
        window.localStorage.setItem(key = key, value = value)

    override fun clear(key: String) =
        window.localStorage.removeItem(key = key)
}
