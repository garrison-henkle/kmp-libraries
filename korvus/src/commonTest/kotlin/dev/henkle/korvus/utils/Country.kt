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
@Serializable(with = Country.Serializer::class)
data class Country(
    @Transient
    override val id: String = generateId(),
    val name: String,
    @Transient
    override val changeVector: String? = null,
    @Transient
    override val lastModified: String? = null,
) : KorvusDocument<Country> {
    @Transient
    override val collection: String = Companion.name

    override fun update(id: String, changeVector: String?, lastModified: String?, collection: String?): Country =
        copy(id = id, changeVector = changeVector, lastModified = lastModified)

    class Serializer : KorvusDocumentJsonSerializer<Country>(
        docSerializer = KorvusDocumentSerializer(
            tSerializer = generatedSerializer(),
        ),
    )

    companion object : KorvusCollection<Country> {
        override val name: String = "Country"
    }
}