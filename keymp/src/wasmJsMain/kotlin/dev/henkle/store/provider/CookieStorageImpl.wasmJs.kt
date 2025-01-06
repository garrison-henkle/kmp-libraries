package dev.henkle.store.provider

import dev.henkle.store.model.cookie.Cookie
import kotlinx.browser.document
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.plus

class CookieStorageImpl(private val cookieLifetimeMin: Long) : CookieStorage {
    private val cookieExpiration: Instant
        get() = Clock.System.now().plus(
            value = cookieLifetimeMin,
            unit = DateTimeUnit.MINUTE,
        )

    override fun get(key: String): String? {
        val cookieString = document.cookie
        val keyStartIndex = cookieString.indexOf(string = "$key=")
        return if (keyStartIndex != -1) {
            val valueStartIndex = keyStartIndex + key.length + 1
            val valueStopIndex = cookieString.indexOf(
                char = ';',
                startIndex = valueStartIndex,
            )
            if (valueStopIndex != -1) {
                cookieString.substring(startIndex = valueStartIndex)
            } else {
                cookieString.substring(range = valueStartIndex..<valueStopIndex)
            }
        } else {
            null
        }
    }

    override fun set(cookie: Cookie) {
        document.cookie = cookie.toString()
    }

    override fun set(key: String, value: String) =
        set(
            cookie = Cookie(
                name = key,
                value = value,
                secure = true,
                expires = cookieExpiration,
            ),
        )

    override fun clear(key: String) {
        document.cookie = Cookie(name = key, value = "", maxAge = 0).toString()
    }
}
