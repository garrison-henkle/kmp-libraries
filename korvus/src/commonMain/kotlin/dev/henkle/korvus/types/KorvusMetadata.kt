package dev.henkle.korvus.types

import dev.henkle.korvus.utils.generateId
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class KorvusMetadata(
    @EncodeDefault(mode = EncodeDefault.Mode.ALWAYS)
    @SerialName(value = "@id")
    val id: String = generateId(),
    @EncodeDefault(mode = EncodeDefault.Mode.ALWAYS)
    @SerialName(value = "@collection")
    val collection: String? = null,
    @EncodeDefault(mode = EncodeDefault.Mode.NEVER)
    @SerialName(value = "@change-vector")
    val changeVector: String? = null,
    @EncodeDefault(mode = EncodeDefault.Mode.NEVER)
    @SerialName(value = "@last-modified")
    val lastModified: String? = null,
) {
    companion object {
        internal const val KEY = "@metadata"
    }
}
