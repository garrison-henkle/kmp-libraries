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
@Serializable(with = Person.Serializer::class)
data class Person(
    @Transient
    override val id: String = generateId(),
    val firstName: String,
    val lastName: String,
    val country: Include<Country>,
    val series: Include<Series>,
    @Transient
    override val changeVector: String? = null,
    @Transient
    override val lastModified: String? = null,
) : KorvusDocument<Person> {
    @Transient
    override val collection: String = name

    override fun update(
        id: String,
        changeVector: String?,
        lastModified: String?,
        collection: String?,
    ): Person = copy(
        id = id,
        changeVector = changeVector,
        lastModified = lastModified,
    )

    class Serializer : KorvusDocumentJsonSerializer<Person>(
        docSerializer = KorvusDocumentSerializer(
            tSerializer = generatedSerializer(),
        ),
    )

    companion object : KorvusCollection<Person> {
        override val name = "Person"
    }
}
