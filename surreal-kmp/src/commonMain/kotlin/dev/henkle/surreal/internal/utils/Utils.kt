package dev.henkle.surreal.internal.utils

import co.touchlab.kermit.Logger
import dev.henkle.nanoid.NanoId
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlin.math.pow
import kotlin.math.roundToLong

suspend inline fun <T> retryWithExponentialBackoff(
    retries: Int = Int.MAX_VALUE,
    baseDelayMs: Long = 500,
    maxDelayMs: Long = 180_000,
    exponential: Double = 2.0,
    handleException: (ex: Exception) -> Boolean = { false },
    block: () -> T): T
{
    var currentDelay = baseDelayMs
    repeat(retries - 1) { retryNumber ->
        try {
            return block()
        } catch (ex: Exception) {
            val rethrow = handleException(ex)
            if (rethrow) throw ex
            Logger.d("SurrealKMP", throwable = ex) { "Retry failed!" }
        }
        delay(currentDelay)
        currentDelay = (baseDelayMs * exponential.pow(n = retryNumber)).roundToLong().coerceAtMost(maxDelayMs)
    }
    return block()
}

internal expect val IODispatcher: CoroutineDispatcher

@Suppress("UnusedReceiverParameter")
internal val Dispatchers.IO: CoroutineDispatcher get() = IODispatcher

private val ID_ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()
private const val ID_LENGTH = 22
internal val idGenerator = NanoId(
    alphabet = ID_ALPHABET,
    length = ID_LENGTH,
)
