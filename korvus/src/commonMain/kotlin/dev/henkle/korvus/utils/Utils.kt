package dev.henkle.korvus.utils

import dev.henkle.korvus.types.Include
import dev.henkle.korvus.types.KorvusDocument

/**
 * Instructs the database to generate this ID upon insertion
 */
fun generateId() = ""

/**
 * Creates an ID instance with the given string [id]
 */
fun <T: Any> id(id: String): Include.ID<T> = Include.ID(id = id)

/**
 * Creates an [Include.ID] instance with the provided [record]'s [KorvusDocument.id]
 */
fun <T: KorvusDocument<T>> id(record: T): Include.ID<T> = Include.ID(id = record.id)

/**
 * Creates a [Include.Document] instance from the provided [record]
 */
fun <T: KorvusDocument<T>> document(record: T): Include.Document<T> =
    Include.Document(id = record.id, record = record)
