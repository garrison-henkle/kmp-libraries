package dev.henkle.korvus

import dev.henkle.korvus.KorvusResult.Failure
import dev.henkle.korvus.KorvusResult.Success
import dev.henkle.korvus.error.KorvusError

sealed interface KorvusResult<T> {
    data class Success<T>(override val result: T) : KorvusResult<T>

    data class Failure<T>(override val error: KorvusError) : KorvusResult<T>

    val isSuccess: Boolean get() = this is Success<*>
    val isFailure: Boolean get() = this is Failure

    val result: T? get() = (this as? Success)?.result
    val error: KorvusError? get() = (this as? Failure)?.error
}

/**
 * When the result is the success case, the result will be [transform]ed to the new output type.
 * When the result is the failure case, the error will be propagated in a result with the new
 * output type.
 *
 * Any failures that occur within the [transform] block will be propagated as a failure case.
 */
inline fun <T, O> KorvusResult<T>.map(transform: (T) -> O): KorvusResult<O> = when(this) {
    is Success -> {
        try {
            val transformed = transform(result)
            Success(result = transformed)
        } catch (ex: Exception) {
            Failure(error = KorvusError.SDK(ex = ex))
        }
    }
    is Failure -> Failure(error = error)
}

/**
 * Allows for handling of the success and failure cases of this result individually as callbacks
 */
inline fun <T, R>KorvusResult<T>.withResult(
    onSuccess: (result: T) -> R,
    onFailure: (error: KorvusError) -> R,
): R =
    when (this) {
        is Success -> onSuccess(result)
        is Failure -> onFailure(error)
    }

/**
 * Helper method that calls [block] with with the contents of the result's success case, if the
 * result was successful.
 */
inline fun <T> KorvusResult<T>.onSuccess(
    block: (result: T) -> Unit,
) {
    if (this is Success) {
        block(result)
    }
}

/**
 * Helper method that calls [block] with with the contents of the result's failure case, if the
 * result was a failure.
 */
inline fun <T> KorvusResult<T>.onFailure(
    block: (error: KorvusError) -> Unit,
) {
    if (this is Failure) {
        block(error)
    }
}
