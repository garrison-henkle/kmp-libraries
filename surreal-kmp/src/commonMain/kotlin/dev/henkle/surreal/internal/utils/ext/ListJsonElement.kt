package dev.henkle.surreal.internal.utils.ext

import dev.henkle.surreal.errors.DatabaseError
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private const val STATUS_ERROR = "ERR"
private const val KEY_STATUS = "status"
private const val KEY_MSG = "result"

internal fun List<JsonElement>.tryParseDatabaseError(): DatabaseError? =
    try {
        first().jsonObject.let { obj ->
            if(obj[KEY_STATUS]?.jsonPrimitive?.content == STATUS_ERROR) {
                obj[KEY_MSG]?.jsonPrimitive?.content?.let { errMsg ->
                    DatabaseError(message = errMsg)
                }
            } else {
                null
            }
        }
    } catch(_: IllegalArgumentException) {
        null
    }
