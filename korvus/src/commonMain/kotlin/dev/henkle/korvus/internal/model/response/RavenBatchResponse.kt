package dev.henkle.korvus.internal.model.response

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
internal data class RavenBatchResponse(
    @SerialName(value = "Results")
    val results: List<Result>,
) {
    @OptIn(ExperimentalSerializationApi::class)
    @JsonClassDiscriminator(discriminator = "Type")
    @Serializable
    internal sealed interface Result {
        val id: String
        val changeVector: String?

        @SerialName(value = "PUT")
        @Serializable
        data class Put(
            @SerialName(value = "@id")
            override val id: String,
            @SerialName(value = "@collection")
            val collection: String,
            @SerialName(value = "@change-vector")
            override val changeVector: String,
            @SerialName(value = "@last-modified")
            val lastModified: String,
        ) : Result

        @SerialName(value = "DELETE")
        @Serializable
        data class Delete(
            @SerialName(value = "Id")
            override val id: String,
            @SerialName(value = "Deleted")
            val deleted: Boolean,
            @SerialName(value = "ChangeVector")
            override val changeVector: String? = null,
        ) : Result

        @SerialName(value = "PATCH")
        @Serializable
        data class Patch(
            @SerialName(value = "Id")
            override val id: String,
            @SerialName(value = "PatchStatus")
            val status: String,
            @SerialName(value = "ChangeVector")
            override val changeVector: String,
            @SerialName(value = "LastModified")
            val lastModified: String,
        ) : Result
    }
}
