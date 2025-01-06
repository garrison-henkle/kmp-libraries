package dev.henkle.korvus.internal.ext

import dev.henkle.korvus.internal.model.response.RavenBatchResponse
import dev.henkle.korvus.types.DBResult

internal fun RavenBatchResponse.Result.toDBResult(): DBResult =
    when (this) {
        is RavenBatchResponse.Result.Put ->
            DBResult.Put(
                id = id,
                collection = collection,
                changeVector = changeVector,
                lastModified = lastModified,
            )

        is RavenBatchResponse.Result.Delete ->
            DBResult.Delete(
                id = id,
                deleted = deleted,
                changeVector = changeVector,
            )

        is RavenBatchResponse.Result.Patch ->
            DBResult.Patch(
                id = id,
                status = status,
                changeVector = changeVector,
                lastModified = lastModified,
            )
    }

internal fun List<RavenBatchResponse.Result>.toDBResults(): List<DBResult> = map { it.toDBResult() }
