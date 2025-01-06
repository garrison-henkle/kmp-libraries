package dev.henkle.korvus.types

sealed interface DBResult {
    val id: String
    val changeVector: String?

    data class Put(
        override val id: String,
        val collection: String,
        override val changeVector: String,
        val lastModified: String,
    ) : DBResult

    data class Delete(
        override val id: String,
        val deleted: Boolean,
        override val changeVector: String?,
    ) : DBResult

    data class Patch(
        override val id: String,
        val status: String,
        override val changeVector: String,
        val lastModified: String,
    ): DBResult
}
