package dev.henkle.korvus.internal.ext

import dev.henkle.korvus.KorvusResult
import dev.henkle.korvus.error.KorvusError
import dev.henkle.korvus.error.RavenError
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse

internal suspend fun <T> HttpResponse.parseFailure(): KorvusResult.Failure<T> = try {
    val error = body<RavenError>()
    KorvusResult.Failure(error = KorvusError.Raven(error = error))
} catch(ex: Exception) {
    KorvusResult.Failure(error = KorvusError.SDK(ex = ex))
}
