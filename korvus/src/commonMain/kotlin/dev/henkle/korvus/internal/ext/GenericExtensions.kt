package dev.henkle.korvus.internal.ext

import dev.henkle.korvus.error.types.NotJsonObjectException
import dev.henkle.korvus.internal.utils.nullSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.serializer

@Throws(NotJsonObjectException::class)
internal fun <T> T.asJsonObject(serializer: SerializationStrategy<T>): JsonObject =
    nullSerializer.encodeToJsonElement(serializer = serializer, value = this) as? JsonObject
        ?: throw NotJsonObjectException()

@Throws(NotJsonObjectException::class)
internal inline fun <reified T> T.asJsonObject(): JsonObject = asJsonObject(serializer = serializer<T>())

internal fun <T> T.asJsonElement(serializer: SerializationStrategy<T>): JsonElement =
    nullSerializer.encodeToJsonElement(serializer = serializer, value = this)
