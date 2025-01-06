package dev.henkle.surreal.sdk

import dev.henkle.surreal.errors.DatabaseError
import dev.henkle.surreal.errors.SurrealError
import dev.henkle.surreal.sdk.SurrealResult.Failure
import dev.henkle.surreal.sdk.SurrealResult.Success
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

sealed interface SurrealResult<T> {
    data class Success<T>(override val value: T) : SurrealResult<T>
    data class Failure<T>(override val error: SurrealError) : SurrealResult<T>

    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure

    val value: T? get() = (this as? Success)?.value
    val error: SurrealError? get() = (this as? Failure)?.error
}

/**
 * When the result is the success case, the result will be [transform]ed to the new output type.
 * When the result is the failure case, the error will be propagated in a result with the new
 * output type.
 *
 * Any failures that occur within the [transform] block will be propagated as a failure case.
 */
inline fun <T, O> SurrealResult<T>.map(transform: (T) -> O): SurrealResult<O> = when (this) {
    is Success -> {
        try {
            Success(value = transform(value))
        } catch (ex: Exception) {
            Failure(
                error = if (ex is DatabaseError) {
                    SurrealError.DB(error = ex)
                } else {
                    SurrealError.SDK(ex = ex)
                },
            )
        }
    }
    is Failure -> Failure(error = error)
}

/**
 * [map] that requires the [transform] to result in a [SurrealResult].
 *
 * This variant provides a cleaner way of chaining successful results without the
 * deep nesting that can arise from [map].
 */
inline fun <T, O> SurrealResult<T>.then(transform: () -> SurrealResult<O>): SurrealResult<O> = when (this) {
    is Success -> {
        try {
            transform()
        } catch (ex: Exception) {
            Failure(
                error = if (ex is DatabaseError) {
                    SurrealError.DB(error = ex)
                } else {
                    SurrealError.SDK(ex = ex)
                },
            )
        }
    }
    is Failure -> Failure(error = error)
}

/**
 * Maps the provided Failure result of type [T] into another Failure result with type [O]
 */
inline fun <T, O> Failure<T>.mapError(): Failure<O> = Failure(error = error)

/**
 * Allows for handling of the success and failure cases of this result individually as callbacks
 */
inline fun <T, R> SurrealResult<T>.withResult(
    onSuccess: (result: T) -> R,
    onFailure: (error: SurrealError) -> R,
): R =
    when (this) {
        is Success -> onSuccess(value)
        is Failure -> onFailure(error)
    }

/**
 * Helper method that calls [block] with with the contents of the result's success case, if the
 * result was successful.
 */
@OptIn(ExperimentalContracts::class)
inline fun <T, R: Any> SurrealResult<T>.onSuccess(
    block: (result: T) -> R?,
): R? {
    contract {
        returnsNotNull() implies (this@onSuccess is Success)
        returns(null) implies (this@onSuccess is Failure)
    }
    return when (this) {
        is Success -> block(value)
        is Failure -> null
    }
}

/**
 * Helper method that calls [block] with with the contents of the result's failure case, if the
 * result was a failure.
 */
@OptIn(ExperimentalContracts::class)
inline fun <T, R: Any> SurrealResult<T>.onFailure(
    block: (error: SurrealError) -> R?,
): R? {
    contract {
        returnsNotNull() implies (this@onFailure is Failure)
        returns(null) implies (this@onFailure is Success)
    }
    return when (this) {
        is Success -> null
        is Failure -> block(error)
    }
}
