package dev.henkle.surreal.internal.utils.ext

import dev.henkle.surreal.types.SurrealEdgeTable
import dev.henkle.surreal.types.SurrealRecord
import dev.henkle.surreal.types.SurrealEdge
import dev.henkle.surreal.types.SurrealTable
import dev.henkle.surreal.types.Thing
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

fun Map<String, Any?>.toSerializableMap(): Map<String, JsonElement> {
    val out = mutableMapOf<String, JsonElement>()
    forEach { (key, value) ->
        out[key] = convertToJsonElement(value = value)
    }
    return out
}

@OptIn(ExperimentalSerializationApi::class)
private fun convertToJsonElement(value: Any?): JsonElement = when (value) {
    is JsonElement -> value
    null -> JsonNull
    is String -> JsonPrimitive(value = value)
    is Boolean -> JsonPrimitive(value = value)
    is Number -> JsonPrimitive(value = value)
    is Thing<*> -> JsonPrimitive(value = value.idString)
    is SurrealRecord<*> -> JsonPrimitive(value = value.idString)
    is SurrealTable<*> -> JsonPrimitive(value = value.tableName)
    is SurrealEdge<*, *, *> -> JsonPrimitive(value = value.idString)
    is SurrealEdgeTable<*, *, *> -> JsonPrimitive(value = value.tableName)
    is Iterable<*> -> JsonArray(content = value.map(transform = ::convertToJsonElement))
    is UByte -> JsonPrimitive(value = value)
    is UShort -> JsonPrimitive(value = value)
    is UInt -> JsonPrimitive(value = value)
    is ULong -> JsonPrimitive(value = value)
    else -> throw IllegalArgumentException("Value '$value' is not supported by toJsonElementMap()!")
}
