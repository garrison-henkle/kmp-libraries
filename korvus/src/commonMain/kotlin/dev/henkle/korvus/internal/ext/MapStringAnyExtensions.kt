package dev.henkle.korvus.internal.ext

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

@OptIn(ExperimentalSerializationApi::class)
fun Map<String, Any?>.toJsonSafe(): Map<String, JsonElement> {
    val out = mutableMapOf<String, JsonElement>()
    for ((key, value) in this) {
        when (value) {
            null -> JsonNull
            is String -> JsonPrimitive(value = value)
            is Boolean -> JsonPrimitive(value = value)
            is Byte -> JsonPrimitive(value = value)
            is Short -> JsonPrimitive(value = value)
            is Int -> JsonPrimitive(value = value)
            is Long -> JsonPrimitive(value = value)
            is UByte -> JsonPrimitive(value = value)
            is UShort -> JsonPrimitive(value = value)
            is UInt -> JsonPrimitive(value = value)
            is ULong -> JsonPrimitive(value = value)
            is Float -> JsonPrimitive(value = value)
            is Double -> JsonPrimitive(value = value)
            else -> null
        }?.also { out[key] = it }
    }
    return out
}
