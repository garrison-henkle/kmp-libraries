package dev.henkle.compose.ktab.model

/**
 * The companion object of a [Tab]
 */
interface TabCompanion<T: Tab<T>> {
    /**
     * Deserializes a [Tab] instance of type [T] from the provided [string]
     *
     * @return if the string was able to be deserialized, a [DeserializeResult.Ok]
     * containing the tab. Otherwise, a [DeserializeResult.Error] containing the
     * error that occurred during deserialization
     */
    fun deserialize(string: String): DeserializeResult<T>

    /**
     * Represents the result of a tab's deserialization
     */
    sealed interface DeserializeResult<T: Tab<T>> {
        /**
         * Represents a successful deserialization result, where [tab] is the
         * deserialized tab.
         */
        data class Ok<T: Tab<T>>(val tab: T) : DeserializeResult<T>

        /**
         * Represents a failed deserialization attempt, where [ex] was the
         * error that prevented deserialization.
         */
        data class Error<T: Tab<T>>(val ex: Exception) : DeserializeResult<T>
    }
}
