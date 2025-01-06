package dev.henkle.stytch.model.cookie

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime

data class Cookie(
    val name: String,
    val value: String,
    val domain: String? = null,
    val path: String? = null,
    val expires: Instant? = null,
    val maxAge: Long? = null,
    val httpOnly: Boolean = false,
    val secure: Boolean = false,
    val sameSite: SameSite = SameSite.Lax,
) {
    private val expiresString = expires
        ?.toLocalDateTime(timeZone = TimeZone.UTC)
        ?.format(format = jsDateFormat)

    private val string = StringBuilder().apply {
        append(name)
        append('=')
        append(value)
        if (domain != null) {
            append(SEPARATOR)
            append(DOMAIN)
            append(domain)
        }
        if (path != null) {
            append(SEPARATOR)
            append(PATH)
            append(path)
        }
        if (expiresString != null) {
            append(SEPARATOR)
            append(EXPIRES)
            append(expiresString)
        }
        if (maxAge != null) {
            append(SEPARATOR)
            append(MAX_AGE)
            append(maxAge)
        }
        if (httpOnly) {
            append(SEPARATOR)
            append(HTTP_ONLY)
        }
        if (secure) {
            append(SEPARATOR)
            append(SECURE)
        }
        if (sameSite != SameSite.Lax) {
            append(SEPARATOR)
            append(SAME_SITE)
            append(sameSite.name)
        }
    }.toString()

    override fun toString(): String = string

    companion object {
        private const val SEPARATOR = "; "
        private const val DOMAIN = "Domain="
        private const val PATH = "Path="
        private const val EXPIRES = "Expires="
        private const val MAX_AGE = "Max-Age="
        private const val HTTP_ONLY = "HttpOnly"
        private const val SECURE = "Secure"
        private const val SAME_SITE = "SameSite="

        // This formats a UTC LocalDateTime in the JavaScript Date.toUTCString() format (RFC-7231)
        private val jsDateFormat = LocalDateTime.Format {
            dayOfWeek(names = DayOfWeekNames.ENGLISH_ABBREVIATED)
            chars(", ")
            dayOfMonth()
            char(' ')
            monthName(names = MonthNames.ENGLISH_ABBREVIATED)
            char(' ')
            year()
            char(' ')
            hour()
            char(':')
            minute()
            char(':')
            second()
            chars(" GMT")
        }
    }
}
