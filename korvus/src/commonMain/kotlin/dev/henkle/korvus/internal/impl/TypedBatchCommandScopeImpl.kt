package dev.henkle.korvus.internal.impl

import dev.henkle.korvus.error.types.MismatchedListParameterLengthsException
import dev.henkle.korvus.internal.ext.asJsonObject
import dev.henkle.korvus.internal.model.RavenBatchCommand
import dev.henkle.korvus.internal.model.RavenBatchCommand.PatchDocument.PatchScript
import dev.henkle.korvus.internal.model.request.RavenBatchRequest
import dev.henkle.korvus.types.TypedBatchCommandScope
import kotlinx.serialization.json.JsonElement
import kotlin.reflect.KType

internal class TypedBatchCommandScopeImpl<T: Any>(
    private val commands: MutableList<JsonElement> = mutableListOf(),
    private val type: KType,
) : TypedBatchCommandScope<T> {
    val request: RavenBatchRequest get() = RavenBatchRequest(commands = commands)

    override suspend fun put(document: T, id: String, changeVector: String?) {
        commands += RavenBatchCommand.PutDocument.createJsonElement(
            document = document,
            id = id,
            changeVector = changeVector,
            type = type,
        )
    }

    override suspend fun put(
        documents: Collection<T>,
        ids: Collection<String>,
        changeVectors: Collection<String?>,
    ) {
        if (documents.size != ids.size || (changeVectors.isNotEmpty() && documents.size != changeVectors.size)) {
            throw MismatchedListParameterLengthsException()
        }
        val document = documents.iterator()
        val id = ids.iterator()
        val changeVector = changeVectors.iterator()
        for (i in documents.indices) {
            commands += RavenBatchCommand.PutDocument.createJsonElement(
                document = document.next(),
                id = id.next(),
                changeVector = if (changeVectors.isNotEmpty()) {
                    changeVector.next()
                } else {
                    null
                },
                type = type,
            )
        }
    }

    override suspend fun delete(id: String, changeVector: String?) {
        commands += RavenBatchCommand.DeleteDocument.createJsonElement<T>(
            id = id,
            changeVector = changeVector,
            type = type,
        )
    }

    override suspend fun delete(ids: Collection<String>, changeVectors: Collection<String?>) {
        if (changeVectors.isNotEmpty() && ids.size != changeVectors.size) {
            throw MismatchedListParameterLengthsException()
        }
        val id = ids.iterator()
        val changeVector = changeVectors.iterator()
        for (i in ids.indices) {
            commands += RavenBatchCommand.DeleteDocument.createJsonElement<T>(
                id = id.next(),
                changeVector = if (changeVectors.isNotEmpty()) {
                    changeVector.next()
                } else {
                    null
                },
                type = type,
            )
        }
    }

    override suspend fun deleteByIDPrefix(prefix: String) {
        commands += RavenBatchCommand.DeleteDocumentsByPrefix.createJsonElement<T>(
            prefix = prefix,
            type = type,
        )
    }

    override suspend fun patch(
        id: String,
        patchScript: String,
        arguments: Map<String, Any?>,
        changeVector: String?,
    ) {
        commands += RavenBatchCommand.PatchDocument.createJsonElement<T>(
            id = id,
            patch = PatchScript(
                script = patchScript,
                arguments = arguments.takeIf { it.isNotEmpty() }?.asJsonObject(),
            ),
            changeVector = changeVector,
            type = type,
        )
    }

    override suspend fun patch(
        ids: Collection<String>,
        patchScripts: Collection<String>,
        arguments: Collection<Map<String, Any?>>,
        changeVectors: Collection<String?>,
    ) {
        if (
            ids.size != patchScripts.size ||
            (arguments.isNotEmpty() && ids.size != arguments.size) ||
            (changeVectors.isNotEmpty() && ids.size != changeVectors.size)
        ) {
            throw MismatchedListParameterLengthsException()
        }
        val id = ids.iterator()
        val patchScript = patchScripts.iterator()
        val args = arguments.iterator()
        val changeVector = changeVectors.iterator()
        for (i in ids.indices) {
            commands += RavenBatchCommand.PatchDocument.createJsonElement<T>(
                id = id.next(),
                patch = PatchScript(
                    script = patchScript.next(),
                    arguments = if (arguments.isNotEmpty()) {
                        args.next().takeIf { it.isNotEmpty() }?.asJsonObject()
                    } else {
                        null
                    },
                ),
                changeVector = if (changeVectors.isNotEmpty()) {
                    changeVector.next()
                } else {
                    null
                },
                type = type,
            )
        }
    }

    override suspend fun patch(
        ids: Collection<String>,
        patchScript: String,
        arguments: Collection<Map<String, Any?>>,
        changeVectors: Collection<String?>,
    ) {
        if (
            (arguments.isNotEmpty() && ids.size != arguments.size) ||
            (changeVectors.isNotEmpty() && ids.size != changeVectors.size)
        ) {
            throw MismatchedListParameterLengthsException()
        }
        val id = ids.iterator()
        val args = arguments.iterator()
        val changeVector = changeVectors.iterator()
        for (i in ids.indices) {
            commands += RavenBatchCommand.PatchDocument.createJsonElement<T>(
                id = id.next(),
                patch = PatchScript(
                    script = patchScript,
                    arguments = if (arguments.isNotEmpty()) {
                        args.next().takeIf { it.isNotEmpty() }?.asJsonObject()
                    } else {
                        null
                    },
                ),
                changeVector = if (changeVectors.isNotEmpty()) {
                    changeVector.next()
                } else {
                    null
                },
                type = type,
            )
        }
    }
}
