package dev.henkle.surreal.internal.utils.ext

import dev.henkle.surreal.internal.utils.nullSerializer
import dev.henkle.surreal.types.SurrealEdge
import dev.henkle.surreal.types.SurrealRecord
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.serializer
import kotlin.reflect.KType

@Suppress("UNCHECKED_CAST")
internal fun <I, O, E> E.encodeToJsonObjWithoutIds(
    type: KType,
): JsonObject where
    I: SurrealRecord<I>,
    O: SurrealRecord<O>,
    E: SurrealEdge<E, I, O> =
        encodeToJsonObjWithoutIds(serializer = serializer(type = type) as KSerializer<E>)

internal fun <I, O, E> E.encodeToJsonObjWithoutIds(
    serializer: KSerializer<E>,
): JsonObject where
    I: SurrealRecord<I>,
    O: SurrealRecord<O>,
    E: SurrealEdge<E, I, O> =
    nullSerializer.encodeToJsonElement(serializer = serializer, value = this)
        .jsonObject
        .toMutableMap()
        .apply {
            remove(key = "id")
            remove(key = "in")
            remove(key = "out")
        }.let(::JsonObject)
