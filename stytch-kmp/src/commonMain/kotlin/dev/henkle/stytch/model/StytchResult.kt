package dev.henkle.stytch.model

sealed interface StytchResult<T : StytchResponseData> {
    data class Success<T : StytchResponseData>(val result: T) : StytchResult<T>
    data class Failure<T : StytchResponseData>(val error: StytchError) : StytchResult<T>

    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure

    fun <R> withResult(
        onSuccess: (T) -> R,
        onFailure: (StytchError) -> R,
    ): R =
        when (this) {
            is Success<T> -> onSuccess(result)
            is Failure<T> -> onFailure(error)
        }
}
