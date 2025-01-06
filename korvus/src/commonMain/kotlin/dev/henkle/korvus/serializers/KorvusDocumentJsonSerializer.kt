package dev.henkle.korvus.serializers

import dev.henkle.korvus.types.KorvusDocument
import dev.henkle.korvus.types.KorvusMetadata
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.putJsonObject

open class KorvusDocumentJsonSerializer<T: KorvusDocument<T>>(
    docSerializer: KorvusDocumentSerializer<T>,
) : JsonTransformingSerializer<T>(tSerializer = docSerializer) {
    override fun transformDeserialize(element: JsonElement): JsonElement =
        buildJsonObject root@{
            putJsonObject(key = KorvusDocument.KEY) type@{
                element.jsonObject.forEach { (key, elm) ->
                    if (key == KorvusMetadata.KEY) {
                        this@root.put(key = key, element = elm)
                    } else {
                        this@type.put(key = key, element = elm)
                    }
                }
            }
        }

    override fun transformSerialize(element: JsonElement): JsonElement =
        buildJsonObject {
            val original = element.jsonObject
            val type = original[KorvusDocument.KEY]
                ?: throw SerializationException(
                    "Unable to find '${KorvusDocument.KEY}' while serializing KorvusDocument!",
                )
            val metadata = original[KorvusMetadata.KEY]
                ?: throw SerializationException(
                    "Unable to find '${KorvusMetadata.KEY}' while serializing KorvusDocument!",
                )

            for ((key, elm) in type.jsonObject) {
                put(key = key, element = elm)
            }
            put(key = KorvusMetadata.KEY, element = metadata)
        }
}
