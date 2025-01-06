package dev.henkle.surreal.internal.utils.ext

import dev.henkle.surreal.types.SurrealIdentifiable
import dev.henkle.surreal.types.SurrealRecord
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonUnquotedLiteral

@OptIn(ExperimentalSerializationApi::class)
internal fun <R: SurrealRecord<R>, I: SurrealIdentifiable<R>> I.encodeToRecordLink(): JsonPrimitive =
    JsonUnquotedLiteral(value = "r\"$idString\"")
