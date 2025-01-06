package dev.henkle.korvus.types

interface KorvusDocument<T> where T : KorvusDocument<T> {
    val id: String
    val changeVector: String?
    val lastModified: String?
    val collection: String?

    fun update(
        id: String = this.id,
        changeVector: String? = this.changeVector,
        lastModified: String? = this.lastModified,
        collection: String? = this.collection
    ): T

    val metadata: KorvusMetadata
        get() = KorvusMetadata(
            id = id,
            changeVector = changeVector,
            lastModified = lastModified,
            collection = collection,
        )

    companion object {
        internal const val KEY = "type"
    }
}
