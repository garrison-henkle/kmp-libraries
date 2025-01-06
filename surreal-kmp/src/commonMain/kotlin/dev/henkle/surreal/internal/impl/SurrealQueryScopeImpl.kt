package dev.henkle.surreal.internal.impl

import dev.henkle.surreal.errors.EmptyArgListException
import dev.henkle.surreal.internal.model.Statement
import dev.henkle.surreal.internal.utils.ext.encodeToJsonObjWithoutId
import dev.henkle.surreal.internal.utils.ext.encodeToJsonObjWithoutIds
import dev.henkle.surreal.internal.utils.ext.encodeToJsonObjWithoutTablePrefixOnId
import dev.henkle.surreal.internal.utils.ext.encodeToRecordLink
import dev.henkle.surreal.internal.utils.nullSerializer
import dev.henkle.surreal.sdk.SurrealLiveQueryHandle
import dev.henkle.surreal.sdk.SurrealLiveQueryResponse
import dev.henkle.surreal.sdk.SurrealQueryScope
import dev.henkle.surreal.sdk.SurrealResult
import dev.henkle.surreal.sdk.SurrealResultPromise
import dev.henkle.surreal.types.SurrealEdge
import dev.henkle.surreal.types.SurrealEdgeTable
import dev.henkle.surreal.types.SurrealIdentifiable
import dev.henkle.surreal.types.SurrealRecord
import dev.henkle.surreal.types.SurrealTable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer
import kotlin.reflect.KType

internal class SurrealQueryScopeImpl : SurrealQueryScope {
    private val queryBuilder = StringBuilder()
    private val statements = mutableListOf<Statement>()
    private val params = mutableMapOf<String, Any>()
    private var currentParamNumber = 0
        get() = field++
    private var statementIndex = 0
        get() = field++
    val query: Query get() = Query(queryString = queryBuilder.toString(), ops = statements, params = params)

    override fun <R : SurrealRecord<R>, I : SurrealIdentifiable<R>> get(id: I, type: KType): SurrealResultPromise<R?> {
        queryBuilder.appendStatement("select * from ${id.tableName} where id = ${param(id)}")
        return Statement.GetById(type = type).let { statement ->
            statements += statement
            SurrealResultPromise(statement = statement, index = statementIndex)
        }
    }

    override fun <R : SurrealRecord<R>, I : SurrealIdentifiable<R>> get(ids: Collection<I>, type: KType): SurrealResultPromise<List<R>> {
        if (ids.isEmpty()) throw EmptyArgListException(function = "get(Collection<I>, KType)")
        queryBuilder.appendStatement("select * from ${ids.first().tableName} where id in ${param(ids)}")
        return Statement.GetByIds(type = type).let { statement ->
            statements += statement
            SurrealResultPromise(statement = statement, index = statementIndex)
        }
    }

    override fun <R : SurrealRecord<R>, T : SurrealTable<R>> getAll(table: T, type: KType): SurrealResultPromise<List<R>> {
        queryBuilder.appendStatement("select * from ${table.tableName}")
        return Statement.GetAll(type = type).let { statement ->
            statements += statement
            SurrealResultPromise(statement = statement, index = statementIndex)
        }
    }

    override fun <R : SurrealRecord<R>> insert(record: R, type: KType): SurrealResultPromise<R> {
        if (record.idString.isEmpty()) {
            queryBuilder.appendStatement("insert into ${record.tableName} ${param(record.encodeToJsonObjWithoutId(type = type))}")
        } else {
            queryBuilder.appendStatement("create ${param(record.encodeToRecordLink())} content ${param(record.encodeToJsonObjWithoutId(type = type))}")
        }
        return Statement.Insert(type = type).let { statement ->
            statements += statement
            SurrealResultPromise(statement = statement, index = statementIndex)
        }
    }

    override fun <R : SurrealRecord<R>> insert(records: Collection<R>, type: KType): SurrealResultPromise<List<R>> {
        if (records.isEmpty()) throw EmptyArgListException(function = "insert(Collection<R>, KType)")
        val recordsWithoutTablePrefixOnId = records.map { it.encodeToJsonObjWithoutTablePrefixOnId(type = type) }
        queryBuilder.appendStatement("insert into ${records.first().tableName} ${param(recordsWithoutTablePrefixOnId)}")
        return Statement.BulkInsert(type = type).let { statement ->
            statements += statement
            SurrealResultPromise(statement = statement, index = statementIndex)
        }
    }

    override fun <R : SurrealRecord<R>> put(record: R, type: KType): SurrealResultPromise<R> {
        queryBuilder.appendStatement("upsert ${param(record.encodeToRecordLink())} content ${param(record.encodeToJsonObjWithoutId(type = type))}")
        return Statement.Put(type = type).let { statement ->
            statements += statement
            SurrealResultPromise(statement = statement, index = statementIndex)
        }
    }

    override fun <R : SurrealRecord<R>> update(record: R, type: KType): SurrealResultPromise<R?> {
        queryBuilder.appendStatement("update ${param(record.encodeToRecordLink())} content ${param(record.encodeToJsonObjWithoutId(type = type))}")
        return Statement.Update(type = type).let { statement ->
            statements += statement
            SurrealResultPromise(statement = statement, index = statementIndex)
        }
    }

    override fun <R : SurrealRecord<R>, T : SurrealTable<R>, U : Any> updateAll(
        table: T,
        update: U,
        recordType: KType,
        updateType: KType
    ): SurrealResultPromise<List<R>> {
        val serializedUpdate = nullSerializer.encodeToJsonElement(serializer = serializer(type = updateType), value = update)
        queryBuilder.appendStatement("update ${table.tableName} content ${param(serializedUpdate)}")
        return Statement.UpdateAll(type = recordType).let { statement ->
            statements += statement
            SurrealResultPromise(statement = statement, index = statementIndex)
        }
    }

    override fun <R : SurrealRecord<R>, I : SurrealIdentifiable<R>, U : Any> merge(
        id: I,
        update: U,
        recordType: KType,
        updateType: KType,
    ): SurrealResultPromise<R?> {
        val serializedUpdate = nullSerializer.encodeToJsonElement(serializer = serializer(type = updateType), value = update)
        queryBuilder.appendStatement("update ${param(id.encodeToRecordLink())} merge ${param(serializedUpdate)}")
        return Statement.Merge(type = recordType).let { statement ->
            statements += statement
            SurrealResultPromise(statement = statement, index = statementIndex)
        }
    }

    override fun <R : SurrealRecord<R>, T : SurrealTable<R>, U : Any> mergeAll(
        table: T,
        update: U,
        recordType: KType,
        updateType: KType,
    ): SurrealResultPromise<List<R>> {
        val serializedUpdate = nullSerializer.encodeToJsonElement(serializer = serializer(type = updateType), value = update)
        queryBuilder.appendStatement("update ${table.tableName} merge ${param(serializedUpdate)}")
        return Statement.MergeAll(type = recordType).let { statement ->
            statements += statement
            SurrealResultPromise(statement = statement, index = statementIndex)
        }
    }

    override fun <R : SurrealRecord<R>, I : SurrealIdentifiable<R>> delete(id: I, type: KType): SurrealResultPromise<R?> {
        queryBuilder.appendStatement("delete ${param(id.encodeToRecordLink())} return before")
        return Statement.DeleteById(type = type).let { statement ->
            statements += statement
            SurrealResultPromise(statement = statement, index = statementIndex)
        }
    }

    override fun <R : SurrealRecord<R>, I : SurrealIdentifiable<R>> delete(ids: Collection<I>, type: KType): SurrealResultPromise<List<R>> {
        if (ids.isEmpty()) throw EmptyArgListException(function = "delete(Collection<R>, KType)")
        queryBuilder.appendStatement("delete ${ids.first().tableName} where id in ${param(ids)} return before")
        return Statement.DeleteByIds(type = type).let { statement ->
            statements += statement
            SurrealResultPromise(statement = statement, index = statementIndex)
        }
    }

    override fun <R : SurrealRecord<R>, T : SurrealTable<R>> deleteAll(table: T, type: KType): SurrealResultPromise<List<R>> {
        queryBuilder.appendStatement("delete ${table.tableName} return before")
        return Statement.DeleteAll(type = type).let { statement ->
            statements += statement
            SurrealResultPromise(statement = statement, index = statementIndex)
        }
    }

    override fun <T: Any> queryOne(query: String, parameters: Map<String, Any>, type: KType): SurrealResultPromise<T> {
        queryBuilder.appendStatement(query)
        params += parameters
        return Statement.QueryOne(type = type).let { statement ->
            statements += statement
            SurrealResultPromise(statement = statement, index = statementIndex)
        }
    }

    override fun <T: Any> queryMany(query: String, parameters: Map<String, Any>, type: KType): SurrealResultPromise<List<T>> {
        queryBuilder.appendStatement(query)
        params += parameters
        return Statement.QueryMany(type = type).let { statement ->
            statements += statement
            SurrealResultPromise(statement = statement, index = statementIndex)
        }
    }

    override fun statement(
        statement: String,
        parameters: Map<String, Any>,
        hasResult: Boolean,
    ): SurrealResultPromise<List<JsonElement>?> {
        queryBuilder.appendStatement(statement)
        params += parameters
        if (hasResult) statements += Statement.RawStatement
        return SurrealResultPromise(statement = Statement.RawStatement, index = statementIndex)
    }

    override fun <I, II, O, OI, E, ET> relate(
        table: ET,
        `in`: II,
        out: OI,
        edgeType: KType
    ): SurrealResultPromise<E> where
        I: SurrealRecord<I>,
        II: SurrealIdentifiable<I>,
        O: SurrealRecord<O>,
        OI: SurrealIdentifiable<O>,
        E: SurrealEdge<E, I, O>,
        ET: SurrealEdgeTable<E, I, O> {
            queryBuilder.appendStatement("relate ${param(`in`)}->${table.tableName}->${param(out)}")
            return Statement.Relate(type = edgeType).let { statement ->
                statements += statement
                SurrealResultPromise(statement = statement, index = statementIndex)
            }
        }

    override fun <I : SurrealRecord<I>, O : SurrealRecord<O>, E : SurrealEdge<E, I, O>> relate(
        edge: E,
        edgeType: KType,
    ): SurrealResultPromise<E> {
        val content = param(edge.encodeToJsonObjWithoutIds(type = edgeType))
        queryBuilder.appendStatement(
            "relate ${param(edge.`in`)}->${edge.tableName}->${param(edge.out)} content $content",
        )
        return Statement.Relate(type = edgeType).let { statement ->
            statements += statement
            SurrealResultPromise(statement = statement, index = statementIndex)
        }
    }

    override fun <R : SurrealRecord<R>, T : SurrealTable<R>> live(
        table: T,
        type: KType
    ): SurrealResultPromise<SurrealLiveQueryResponse<R>> {
        queryBuilder.appendStatement("live select * from ${table.tableName}")
        return Statement.Live(type = type).let { statement ->
            statements += statement
            SurrealResultPromise(statement = statement, index = statementIndex)
        }
    }

    override fun kill(handle: SurrealLiveQueryHandle): SurrealResultPromise<Unit> {
        queryBuilder.appendStatement("kill ${param(handle)}")
        return Statement.Kill(handle = handle).let { statement ->
            statements += statement
            SurrealResultPromise(statement = statement, index = statementIndex)
        }
    }

    override fun beginTransaction() {
        queryBuilder.appendStatement("begin transaction")
    }

    override fun commitTransaction() {
        queryBuilder.appendStatement("commit transaction")
    }

    private fun param(value: Any): String {
        val paramName = "$PARAM_NAME$currentParamNumber"
        params += paramName to value
        return "$$paramName"
    }

    data class Query(val queryString: String, val ops: List<Statement>, val params: Map<String, Any>)

    private fun StringBuilder.appendStatement(statement: String) {
        append(statement)
        append("; ")
    }

    companion object {
        private const val PARAM_NAME = "p"
    }
}
