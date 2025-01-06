package dev.henkle.korvus.ext

import dev.henkle.korvus.types.KorvusDocument
import dev.henkle.korvus.types.TypedBatchCommandScope
import kotlinx.serialization.json.JsonElement

/**
 * Inserts [document] into the database if it doesn't exist, or updates the existing document in the database
 * if it does exist.
 *
 * @param document the document to write to the database
 *
 * @return a result that indicates success when the update succeeded or failure otherwise
 */
suspend inline fun <reified T: KorvusDocument<T>> TypedBatchCommandScope<T>.put(document: T) =
    put(
        document = document,
        id = document.metadata.id,
        changeVector = document.metadata.changeVector,
    )

/**
 * Inserts the [documents] into the database if they do not exist, or updates the existing documents in the
 * database if they do exist.
 *
 * @param documents the list of documents to write to the database
 *
 * @return a result that indicates success when all database operations succeeded or failure otherwise
 */
suspend inline fun <reified T: KorvusDocument<T>> TypedBatchCommandScope<T>.put(
    documents: Collection<T>,
) = put(
    documents = documents,
    ids = documents.map { it.metadata.id },
    changeVectors = documents.map { it.metadata.changeVector },
)

/**
 * Deletes a document from the database.
 *
 * @param document the document to delete
 */
suspend inline fun <reified T: KorvusDocument<T>> TypedBatchCommandScope<T>.delete(document: T) =
    delete(id = document.metadata.id, changeVector = document.metadata.changeVector)

/**
 * Deletes documents from the database.
 *
 * @param documents the documents to delete
 */
suspend inline fun <reified T: KorvusDocument<T>> TypedBatchCommandScope<T>.delete(
    documents: Collection<T>,
) = delete(
    ids = documents.map { it.metadata.id },
    changeVectors = documents.map { it.metadata.changeVector },
)


/**
 * Patches a document in the database.
 *
 * @param document the document to patch
 * @param patchScript the JavaScript script that will be used to patch the document. Named arguments can be created
 * by prefixing the argument name with '$' e.g. "this.name = $name"
 * @param arguments the arguments for the [patchScript]. The leading '$' should be excluded. All values should be
 * serializable as [JsonElement]s
 */
suspend inline fun <reified T: KorvusDocument<T>> TypedBatchCommandScope<T>.patch(
    document: T,
    patchScript: String,
    arguments: Map<String, Any?> = emptyMap(),
) = patch(
    id = document.metadata.id,
    patchScript = patchScript,
    arguments = arguments,
    changeVector = document.metadata.changeVector,
)

/**
 * Patches documents in the database.
 *
 * @param documents the documents to patch
 * @param patchScripts the JavaScript scripts that will be used to patch the documents. There should be one patch
 * script for each document in [documents]. Named arguments can be created by prefixing the argument name with '$' e.g.
 * "this.name = $name"
 * @param arguments the arguments for the [patchScripts]. The leading '$' should be excluded. All values should be
 * serializable as [JsonElement]s. There should be one entry arguments list for each document in [documents] OR an empty
 * list if no arguments are needed for any of the scripts
 */
suspend inline fun <reified T: KorvusDocument<T>> TypedBatchCommandScope<T>.patch(
    documents: Collection<T>,
    patchScripts: Collection<String>,
    arguments: Collection<Map<String, Any?>> = emptyList(),
) = patch(
    ids = documents.map { it.metadata.id },
    patchScripts = patchScripts,
    arguments = arguments,
    changeVectors = documents.map { it.metadata.changeVector },
)

/**
 * Patches documents in the database.
 *
 * @param documents the documents to patch
 * @param patchScript the JavaScript script that will be used to patch the documents. Named arguments can be created
 * by prefixing the argument name with '$' e.g. "this.name = $name"
 * @param arguments the arguments for the [patchScript]. The leading '$' should be excluded. All values should be
 * serializable as [JsonElement]s. There should be one entry in the arguments list for each document in [documents] OR
 * an empty list if no arguments are needed for any of the scripts
 */
suspend inline fun <reified T: KorvusDocument<T>> TypedBatchCommandScope<T>.patch(
    documents: Collection<T>,
    patchScript: String,
    arguments: Collection<Map<String, Any?>> = emptyList(),
) = patch(
    ids = documents.map { it.metadata.id },
    patchScript = patchScript,
    arguments = arguments,
    changeVectors = documents.map { it.metadata.changeVector },
)