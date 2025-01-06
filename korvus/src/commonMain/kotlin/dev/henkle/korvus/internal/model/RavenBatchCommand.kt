package dev.henkle.korvus.internal.model

import dev.henkle.korvus.internal.model.RavenBatchCommand.PatchDocument.PatchScript
import dev.henkle.korvus.internal.utils.nullSerializer
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlin.reflect.KType

@Suppress("UNCHECKED_CAST")
@OptIn(ExperimentalSerializationApi::class)
@Serializable(with = RavenBatchCommand.Serializer::class)
internal sealed interface RavenBatchCommand<T: Any> {
    @SerialName(value = "Type")
    val type: String

    @ConsistentCopyVisibility
    @Serializable
    data class PutDocument<T: Any> internal constructor(
        @SerialName(value = "Document")
        val document: T,
        @SerialName(value = "Id")
        val id: String = "",
        @EncodeDefault(mode = EncodeDefault.Mode.NEVER)
        @SerialName(value = "ChangeVector")
        val changeVector: String? = null,
        @EncodeDefault(mode = EncodeDefault.Mode.NEVER)
        @SerialName(value = "ForceRevisionCreationStrategy")
        val forceRevisionCreationStrategy: String? = null,
    ) : RavenBatchCommand<T> {
        @SerialName(value = "Type")
        override val type: String = COMMAND_TYPE

        companion object {
            private const val COMMAND_TYPE = "PUT"

            /**
             * When updating an existing document, set to Before to make a revision of the document before it is updated
             */
            @Suppress("unused")
            const val FORCE_REVISION_CREATION_STRATEGY_BEFORE = "Before"

            fun <T: Any> createJsonElement(document: T, id: String, changeVector: String?, type: KType): JsonElement {
                val serializer = serializer(
                    typeSerial0 = kotlinx.serialization.serializer(type = type) as KSerializer<Any>,
                )
                val putDocument = PutDocument(document = document, id = id, changeVector = changeVector)
                return nullSerializer.encodeToJsonElement(
                    serializer = serializer,
                    value = putDocument as PutDocument<Any>,
                )
            }
        }
    }

    @Serializable
    data class PatchDocument<T: Any>(
        @SerialName(value = "Id")
        val id: String,
        @SerialName(value = "Patch")
        val patch: PatchScript,
        @EncodeDefault(mode = EncodeDefault.Mode.NEVER)
        @SerialName(value = "PatchIfMissing")
        val patchIfMissing: PatchScript? = null,
        @EncodeDefault(mode = EncodeDefault.Mode.NEVER)
        @SerialName(value = "ChangeVector")
        val changeVector: String? = null,
    ) : RavenBatchCommand<T> {
        @SerialName(value = "Type")
        override val type: String = COMMAND_TYPE

        /**
         * @property script the JavaScript patch script that will be executed. Arguments in [arguments] can be
         * referenced by prefixing the argument name with a '$' e.g. "this.name = $name"
         * @property arguments arguments that will be used in the script
         */
        @Serializable
        data class PatchScript(
            @SerialName(value = "Script")
            val script: String,
            @EncodeDefault(mode = EncodeDefault.Mode.NEVER)
            @SerialName(value = "Values")
            val arguments: JsonObject? = null,
        )

        companion object {
            private const val COMMAND_TYPE = "PATCH"

            fun <T: Any> createJsonElement(
                id: String,
                patch: PatchScript,
                changeVector: String?,
                type: KType,
            ): JsonElement {
                val serializer = serializer(
                    typeSerial0 = kotlinx.serialization.serializer(type = type) as KSerializer<Any>,
                )
                val patchDocument = PatchDocument<T>(id = id, patch = patch, changeVector = changeVector)
                return nullSerializer.encodeToJsonElement(
                    serializer = serializer,
                    value = patchDocument as PatchDocument<Any>,
                )
            }
        }
    }

    @Serializable
    data class DeleteDocument<T: Any>(
        @SerialName(value = "Id")
        val id: String,
        @EncodeDefault(mode = EncodeDefault.Mode.NEVER)
        @SerialName(value = "ChangeVector")
        val changeVector: String? = null,
    ) : RavenBatchCommand<T> {
        @SerialName(value = "Type")
        override val type: String = COMMAND_TYPE
        companion object {
            private const val COMMAND_TYPE = "DELETE"

            fun <T: Any> createJsonElement(id: String, changeVector: String?, type: KType): JsonElement {
                val serializer = serializer(
                    typeSerial0 = kotlinx.serialization.serializer(type = type) as KSerializer<Any>,
                )
                val deleteDocument = DeleteDocument<T>(id = id, changeVector = changeVector)
                return nullSerializer.encodeToJsonElement(
                    serializer = serializer,
                    value = deleteDocument as DeleteDocument<Any>,
                )
            }
        }
    }

    /**
     * Deletes all documents whose ids are prefixed with [prefix]
     */
    @Serializable
    data class DeleteDocumentsByPrefix<T: Any>(
        @SerialName(value = "Id")
        val prefix: String,
    ) : RavenBatchCommand<T> {
        @Suppress("unused")
        @SerialName(value = "IdPrefixed")
        val idPrefixed = ID_PREFIXED_DELETE
        @SerialName(value = "Type")
        override val type: String = COMMAND_TYPE
        companion object {
            private const val COMMAND_TYPE = "DELETE"
            private const val ID_PREFIXED_DELETE = true

            fun <T: Any> createJsonElement(prefix: String, type: KType): JsonElement {
                val serializer = serializer(
                    typeSerial0 = kotlinx.serialization.serializer(type = type) as KSerializer<Any>,
                )
                val deleteDocument = DeleteDocumentsByPrefix<T>(prefix = prefix)
                return nullSerializer.encodeToJsonElement(
                    serializer = serializer,
                    value = deleteDocument as DeleteDocumentsByPrefix<Any>,
                )
            }
        }
    }

    class Serializer<T: Any>(
        genericSerializer: KSerializer<T>,
    ) : KSerializer<RavenBatchCommand<T>> {
        @Serializable
        data class Surrogate<T: Any>(
            @SerialName(value = "Type")
            val type: String,
            @EncodeDefault(mode = EncodeDefault.Mode.NEVER)
            @SerialName(value = "Document")
            val document: T? = null,
            @EncodeDefault(mode = EncodeDefault.Mode.NEVER)
            @SerialName(value = "Id")
            val id: String? = null,
            @EncodeDefault(mode = EncodeDefault.Mode.NEVER)
            @SerialName(value = "ChangeVector")
            val changeVector: String? = null,
            @EncodeDefault(mode = EncodeDefault.Mode.NEVER)
            @SerialName(value = "ForceRevisionCreationStrategy")
            val forceRevisionCreationStrategy: String? = null,
            @EncodeDefault(mode = EncodeDefault.Mode.NEVER)
            @SerialName(value = "Patch")
            val patch: PatchScript? = null,
            @EncodeDefault(mode = EncodeDefault.Mode.NEVER)
            @SerialName(value = "PatchIfMissing")
            val patchIfMissing: PatchScript? = null,
            @EncodeDefault(mode = EncodeDefault.Mode.NEVER)
            @SerialName(value = "IdPrefixed")
            val idPrefixed: Boolean? = null
        )

        private val surrogateSerializer = Surrogate.serializer(typeSerial0 = genericSerializer)

        override val descriptor: SerialDescriptor = surrogateSerializer.descriptor

        override fun serialize(encoder: Encoder, value: RavenBatchCommand<T>) {
            val surrogate: Surrogate<T> = when (value) {
                is PutDocument<T> -> Surrogate(
                    type = value.type,
                    document = value.document,
                    id = value.id,
                    changeVector = value.changeVector,
                    forceRevisionCreationStrategy = value.forceRevisionCreationStrategy,
                )
                is PatchDocument<T> -> Surrogate(
                    type = value.type,
                    id = value.id,
                    patch = value.patch,
                    patchIfMissing = value.patchIfMissing,
                    changeVector = value.changeVector,
                )
                is DeleteDocument<T> -> Surrogate(
                    type = value.type,
                    id = value.id,
                    changeVector = value.changeVector,
                )
                is DeleteDocumentsByPrefix<T> -> Surrogate(
                    type = value.type,
                    id = value.prefix,
                    idPrefixed = value.idPrefixed,
                )
            }
            encoder.encodeSerializableValue(
                serializer = surrogateSerializer,
                value = surrogate,
            )
        }

        override fun deserialize(decoder: Decoder): RavenBatchCommand<T> {
            throw NotImplementedError("Deserialization is not supported for RavenBatchCommand!")
        }
    }
}
