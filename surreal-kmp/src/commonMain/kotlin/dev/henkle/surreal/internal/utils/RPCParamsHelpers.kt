package dev.henkle.surreal.internal.utils

import dev.henkle.surreal.internal.model.RPCParams

internal fun strings(vararg params: String?): RPCParams.DataList<String?> = RPCParams.DataList(data = params.toList())

internal fun <T> obj(param: T): RPCParams.DataList<T> = RPCParams.DataList(data = listOf(param))

internal fun <T> stringWithObj(string: String, param: T): RPCParams.StringWithData<T> =
    RPCParams.StringWithData(string = string, data = param)

internal fun <T> relate(inId: String, edgeTable: String, outId: String, data: T): RPCParams.Relation<T> =
    RPCParams.Relation(inId = inId, edgeTable = edgeTable, outId = outId, data = data)
