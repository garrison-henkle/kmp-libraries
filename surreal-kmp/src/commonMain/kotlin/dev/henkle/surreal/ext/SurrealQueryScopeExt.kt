package dev.henkle.surreal.ext

import dev.henkle.surreal.sdk.SurrealQueryScope
import dev.henkle.surreal.types.SurrealEdgeTable
import dev.henkle.surreal.types.SurrealIdentifiable
import dev.henkle.surreal.types.SurrealRecord
import dev.henkle.surreal.types.SurrealEdge
import dev.henkle.surreal.types.SurrealTable
import kotlin.reflect.typeOf

internal inline fun <reified R: SurrealRecord<R>, I: SurrealIdentifiable<R>> SurrealQueryScope.get(id: I) =
    get(id = id, type = typeOf<R>())

internal inline fun <reified R: SurrealRecord<R>, I: SurrealIdentifiable<R>> SurrealQueryScope.get(ids: Collection<I>) =
    get(ids = ids, type = typeOf<R>())

internal inline fun <reified R: SurrealRecord<R>, T: SurrealTable<R>> SurrealQueryScope.getAll(table: T) =
    getAll(table = table, type = typeOf<R>())

internal inline fun <reified R: SurrealRecord<R>> SurrealQueryScope.insert(record: R) =
    insert(record = record, type = typeOf<R>())

internal inline fun <reified R: SurrealRecord<R>> SurrealQueryScope.insert(records: Collection<R>) =
    insert(records = records, type = typeOf<R>())

// aka upsert
internal inline fun <reified R: SurrealRecord<R>> SurrealQueryScope.put(record: R) =
    put(record = record, type = typeOf<R>())

internal inline fun <reified R: SurrealRecord<R>> SurrealQueryScope.update(record: R) =
    update(record = record, type = typeOf<R>())

internal inline fun <reified R: SurrealRecord<R>, T: SurrealTable<R>, reified U: Any> SurrealQueryScope.updateAll(
    table: T,
    update: U,
) = updateAll(table = table, update = update, recordType = typeOf<R>(), updateType = typeOf<U>())

internal inline fun <reified R: SurrealRecord<R>, I: SurrealIdentifiable<R>, reified U: Any> SurrealQueryScope.merge(
    id: I,
    update: U,
) = merge(id = id, update = update, recordType = typeOf<R>(), updateType = typeOf<U>())

internal inline fun <reified R: SurrealRecord<R>, T: SurrealTable<R>, reified U: Any> SurrealQueryScope.mergeAll(
    table: T,
    update: U,
) = mergeAll(table = table, update = update, recordType = typeOf<R>(), updateType = typeOf<U>())

internal inline fun <reified R: SurrealRecord<R>, I: SurrealIdentifiable<R>> SurrealQueryScope.delete(id: I) =
    delete(id = id, type = typeOf<R>())

internal inline fun <reified R: SurrealRecord<R>, I: SurrealIdentifiable<R>> SurrealQueryScope.delete(ids: Collection<I>) =
    delete(ids = ids, type = typeOf<R>())

internal inline fun <reified R: SurrealRecord<R>, T: SurrealTable<R>> SurrealQueryScope.deleteAll(table: T) =
    deleteAll(table = table, type = typeOf<R>())

internal inline fun <reified T: Any> SurrealQueryScope.queryOne(
    query: String,
    parameters: Map<String, Any> = emptyMap(),
) = queryOne<T>(query = query, parameters = parameters, type = typeOf<T>())

internal inline fun <reified T: Any> SurrealQueryScope.queryMany(
    query: String,
    parameters: Map<String, Any> = emptyMap(),
) = queryMany<T>(query = query, parameters = parameters, type = typeOf<T>())

internal inline fun <reified R: SurrealRecord<R>, T: SurrealTable<R>> SurrealQueryScope.live(
    table: T,
) = live(table = table, type = typeOf<R>())

internal inline fun <I, II, O, OI, reified E, ET> SurrealQueryScope.relate(
    table: ET,
    `in`: II,
    out: OI,
) where I: SurrealRecord<I>,
        II: SurrealIdentifiable<I>,
        O: SurrealRecord<O>,
        OI: SurrealIdentifiable<O>,
        E: SurrealEdge<E, I, O>,
        ET: SurrealEdgeTable<E, I, O> =
            relate(table = table, `in` = `in`, out = out, edgeType = typeOf<E>())

internal inline fun <I: SurrealRecord<I>, O: SurrealRecord<O>, reified E: SurrealEdge<E, I, O>> SurrealQueryScope.relate(edge: E) =
    relate(edge = edge, edgeType = typeOf<E>())
