package dev.henkle.korvus.utils

import dev.henkle.korvus.serializers.KorvusDocumentJsonSerializer
import dev.henkle.korvus.serializers.KorvusDocumentSerializer
import dev.henkle.korvus.types.Include
import dev.henkle.korvus.types.KorvusCollection
import dev.henkle.korvus.types.KorvusDocument
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KeepGeneratedSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Suppress("RUNTIME_ANNOTATION_NOT_SUPPORTED")
@OptIn(ExperimentalSerializationApi::class)
@KeepGeneratedSerializer
@Serializable(with = Series.Serializer::class)
data class Series(
    @Transient
    override val id: String = generateId(),
    val name: String,
    val primaryMedium: String,
    val genre: Include<Genre>,
    @Transient
    override val changeVector: String? = null,
    @Transient
    override val lastModified: String? = null,
) : KorvusDocument<Series> {
    @Transient
    override val collection: String = Companion.name

    override fun update(
        id: String,
        changeVector: String?,
        lastModified: String?,
        collection: String?,
    ): Series = copy(
        id = id,
        changeVector = changeVector,
        lastModified = lastModified,
    )

    class Serializer : KorvusDocumentJsonSerializer<Series>(
        docSerializer = KorvusDocumentSerializer(
            tSerializer = generatedSerializer(),
        ),
    )

    companion object : KorvusCollection<Series> {
        override val name = "Series"
    }
}
