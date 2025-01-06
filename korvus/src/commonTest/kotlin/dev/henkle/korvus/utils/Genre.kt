package dev.henkle.korvus.utils

import dev.henkle.korvus.serializers.KorvusDocumentJsonSerializer
import dev.henkle.korvus.serializers.KorvusDocumentSerializer
import dev.henkle.korvus.types.KorvusCollection
import dev.henkle.korvus.types.KorvusDocument
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KeepGeneratedSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Suppress("RUNTIME_ANNOTATION_NOT_SUPPORTED")
@OptIn(ExperimentalSerializationApi::class)
@KeepGeneratedSerializer
@Serializable(with = Genre.Serializer::class)
data class Genre(
    @Transient
    override val id: String = generateId(),
    val name: String,
    @Transient
    override val changeVector: String? = null,
    @Transient
    override val lastModified: String? = null,
) : KorvusDocument<Genre> {
    @Transient
    override val collection = Companion.name

    override fun update(id: String, changeVector: String?, lastModified: String?, collection: String?): Genre =
        copy(id = id, changeVector = changeVector, lastModified = lastModified)

    class Serializer : KorvusDocumentJsonSerializer<Genre>(
        docSerializer = KorvusDocumentSerializer(
            tSerializer = generatedSerializer(),
        ),
    )

    companion object : KorvusCollection<Genre> {
        override val name: String = "Genre"
    }
}