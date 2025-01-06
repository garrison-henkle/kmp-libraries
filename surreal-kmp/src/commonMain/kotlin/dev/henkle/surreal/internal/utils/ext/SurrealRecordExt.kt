package dev.henkle.surreal.internal.utils.ext

import dev.henkle.surreal.internal.utils.nullSerializer
import dev.henkle.surreal.types.SurrealRecord
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonUnquotedLiteral
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.serializer
import kotlin.reflect.KType

@Suppress("UNCHECKED_CAST")
internal fun <R: SurrealRecord<R>> R.encodeToJsonObjWithoutId(type: KType): JsonObject =
    encodeToJsonObjWithoutId(serializer = serializer(type = type) as KSerializer<R>)

internal fun <R: SurrealRecord<R>> R.encodeToJsonObjWithoutId(serializer: KSerializer<R>): JsonObject =
    nullSerializer.encodeToJsonElement(serializer = serializer, value = this)
        .jsonObject
        .toMutableMap()
        .apply { remove(key = "id") }
        .let(::JsonObject)

internal fun <R: SurrealRecord<R>> R.encodeToJsonObjWithoutTablePrefixOnId(type: KType): JsonObject =
    nullSerializer.encodeToJsonElement(serializer = serializer(type = type), value = this)
        .jsonObject
        .toMutableMap()
        .apply {
            get(key = "id")?.jsonPrimitive?.content?.substringAfter(delimiter = ':')?.also { id ->
                put(key = "id", value = JsonPrimitive(value = id))
            }
        }.let(::JsonObject)

@OptIn(ExperimentalSerializationApi::class)
internal fun <R: SurrealRecord<R>> R.encodeToRecordLink(): JsonPrimitive = JsonUnquotedLiteral(value = "r\"$idString\"")
