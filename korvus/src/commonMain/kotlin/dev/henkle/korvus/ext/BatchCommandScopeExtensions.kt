package dev.henkle.korvus.ext

import dev.henkle.korvus.types.BatchCommandScope
import dev.henkle.korvus.types.KorvusDocument
import dev.henkle.korvus.types.TypedBatchCommandScope
import kotlinx.serialization.json.JsonElement
import kotlin.reflect.typeOf

/**
 * Inserts [document] into the database if it doesn't exist, or updates the existing document in the database
 * if it does exist.
 *
 * @param document the document to write to the database
 * @param id the id to associate with the document, or empty string to allow for the id to be generated
 * @param changeVector the change vector for the document or null if a change vector is not being used
 *
 * @return a result that indicates success when the update succeeded or failure otherwise
 */
suspend inline fun <reified T: Any> BatchCommandScope.put(
    document: T,
    id: String = "",
    changeVector: String? = null,
) = put(document = document, id = id, changeVector = changeVector, type = typeOf<T>())

/**
 * Inserts [document] into the database if it doesn't exist, or updates the existing document in the database
 * if it does exist.
 *
 * @param document the document to write to the database
 *
 * @return a result that indicates success when the update succeeded or failure otherwise
 */
suspend inline fun <reified T: KorvusDocument<T>> BatchCommandScope.put(document: T) =
    put(
        document = document,
        id = document.metadata.id,
        changeVector = document.metadata.changeVector,
        type = typeOf<T>(),
    )

/**
 * Inserts the [documents] into the database if they do not exist, or updates the existing documents in the
 * database if they do exist.
 *
 * @param documents the list of documents to write to the database
 * @param ids the optional ids to use for [documents]. If an empty string, the id will be generated by the database
 * @param changeVectors the change vectors for the ids. There should be on entry in the change vectors list for each
 * id in [ids] OR an empty list if no change vectors are being provided
 *
 * @return a result that indicates success when all database operations succeeded or failure otherwise
 */
suspend inline fun <reified T: Any> BatchCommandScope.put(
    documents: Collection<T>,
    ids: Collection<String> = List(size = documents.size) { "" },
    changeVectors: Collection<String?> = emptyList(),
) = put(
    documents = documents,
    ids = ids,
    changeVectors = changeVectors,
    type = typeOf<T>(),
)

/**
 * Inserts the [documents] into the database if they do not exist, or updates the existing documents in the
 * database if they do exist.
 *
 * @param documents the list of documents to write to the database
 *
 * @return a result that indicates success when all database operations succeeded or failure otherwise
 */
suspend inline fun <reified T: KorvusDocument<T>> BatchCommandScope.put(
    documents: Collection<T>,
) = put(
    documents = documents,
    ids = documents.map { it.metadata.id },
    changeVectors = documents.map { it.metadata.changeVector },
    type = typeOf<T>(),
)

/**
 * Deletes a document from the database.
 *
 * @param id the id of the document to delete
 * @param changeVector the change vector for the document or null if a change vector is not being used
 */
suspend inline fun <reified T: Any> BatchCommandScope.delete(id: String, changeVector: String? = null) =
    delete<T>(id = id, changeVector = changeVector, type = typeOf<T>())

/**
 * Deletes a document from the database.
 *
 * @param document the document to delete
 */
suspend inline fun <reified T: KorvusDocument<T>> BatchCommandScope.delete(document: T) =
    delete<T>(id = document.metadata.id, changeVector = document.metadata.changeVector, type = typeOf<T>())

/**
 * Deletes documents from the database.
 *
 * @param ids the ids of the documents to delete
 * @param changeVectors the change vectors for the ids. There should be on entry in the change vectors list for each
 * id in [ids] OR an empty list if no change vectors are being provided
 */
suspend inline fun <reified T: Any> BatchCommandScope.delete(
    ids: Collection<String>,
    changeVectors: Collection<String?> = emptyList(),
) = delete<T>(ids = ids, changeVectors = changeVectors, type = typeOf<T>())

/**
 * Deletes documents from the database.
 *
 * @param documents the documents to delete
 */
suspend inline fun <reified T: KorvusDocument<T>> BatchCommandScope.delete(
    documents: Collection<T>,
) = delete<T>(
    ids = documents.map { it.metadata.id },
    changeVectors = documents.map { it.metadata.changeVector },
    type = typeOf<T>(),
)


/**
 * Deletes documents from the database whose ids share the provided prefix.
 *
 * @param prefix the prefix of the ids to delete
 */
suspend inline fun <reified T: Any> BatchCommandScope.deleteByIDPrefix(prefix: String) =
    deleteByIDPrefix<T>(prefix = prefix, type = typeOf<T>())

/**
 * Patches a document in the database.
 *
 * @param id the id of the document to patch
 * @param patchScript the JavaScript script that will be used to patch the document. Named arguments can be created
 * by prefixing the argument name with '$' e.g. "this.name = $name"
 * @param arguments the arguments for the [patchScript]. The leading '$' should be excluded. All values should be
 * serializable as [JsonElement]s
 * @param changeVector the change vector for the document or null if a change vector is not being used
 */
suspend inline fun <reified T: Any> BatchCommandScope.patch(
    id: String,
    patchScript: String,
    arguments: Map<String, Any?> = emptyMap(),
    changeVector: String? = null,
) = patch<T>(id = id, patchScript = patchScript, arguments = arguments, changeVector = changeVector, type = typeOf<T>())

/**
 * Patches a document in the database.
 *
 * @param document the document to patch
 * @param patchScript the JavaScript script that will be used to patch the document. Named arguments can be created
 * by prefixing the argument name with '$' e.g. "this.name = $name"
 * @param arguments the arguments for the [patchScript]. The leading '$' should be excluded. All values should be
 * serializable as [JsonElement]s
 */
suspend inline fun <reified T: KorvusDocument<T>> BatchCommandScope.patch(
    document: T,
    patchScript: String,
    arguments: Map<String, Any?> = emptyMap(),
) = patch<T>(
    id = document.metadata.id,
    patchScript = patchScript,
    arguments = arguments,
    changeVector = document.metadata.changeVector,
    type = typeOf<T>(),
)

/**
 * Patches documents in the database.
 *
 * @param ids the ids of the documents to patch
 * @param patchScripts the JavaScript scripts that will be used to patch the documents. There should be one patch
 * script for each id in [ids]. Named arguments can be created by prefixing the argument name with '$' e.g.
 * "this.name = $name"
 * @param arguments the arguments for the [patchScripts]. The leading '$' should be excluded. All values should be
 * serializable as [JsonElement]s. There should be one entry arguments list for each id in [ids] OR an empty list if
 * no arguments are needed for any of the scripts
 * @param changeVectors the change vectors for the ids. There should be on entry in the change vectors list for each
 * id in [ids] OR an empty list if no change vectors are being provided
 */
suspend inline fun <reified T: Any> BatchCommandScope.patch(
    ids: Collection<String>,
    patchScripts: Collection<String>,
    arguments: Collection<Map<String, Any?>> = emptyList(),
    changeVectors: Collection<String?> = emptyList(),
) = patch<T>(
    ids = ids,
    patchScripts = patchScripts,
    arguments = arguments,
    changeVectors = changeVectors,
    type = typeOf<T>(),
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
suspend inline fun <reified T: KorvusDocument<T>> BatchCommandScope.patch(
    documents: Collection<T>,
    patchScripts: Collection<String>,
    arguments: Collection<Map<String, Any?>> = emptyList(),
) = patch<T>(
    ids = documents.map { it.metadata.id },
    patchScripts = patchScripts,
    arguments = arguments,
    changeVectors = documents.map { it.metadata.changeVector },
    type = typeOf<T>(),
)

/**
 * Patches documents in the database.
 *
 * @param ids the ids of the documents to patch
 * @param patchScript the JavaScript script that will be used to patch the documents. Named arguments can be created
 * by prefixing the argument name with '$' e.g. "this.name = $name"
 * @param arguments the arguments for the [patchScript]. The leading '$' should be excluded. All values should be
 * serializable as [JsonElement]s. There should be one entry in the arguments list for each id in [ids] OR an empty
 * list if no arguments are needed for any of the scripts
 * @param changeVectors the change vectors for the ids. There should be on entry in the change vectors list for each
 * id in [ids] OR an empty list if no change vectors are being provided
 */
suspend inline fun <reified T: Any> BatchCommandScope.patch(
    ids: Collection<String>,
    patchScript: String,
    arguments: Collection<Map<String, Any?>> = emptyList(),
    changeVectors: Collection<String?> = emptyList(),
) = patch<T>(
    ids = ids,
    patchScript = patchScript,
    arguments = arguments,
    changeVectors = changeVectors,
    type = typeOf<T>(),
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
suspend inline fun <reified T: KorvusDocument<T>> BatchCommandScope.patch(
    documents: Collection<T>,
    patchScript: String,
    arguments: Collection<Map<String, Any?>> = emptyList(),
) = patch<T>(
    ids = documents.map { it.metadata.id },
    patchScript = patchScript,
    arguments = arguments,
    changeVectors = documents.map { it.metadata.changeVector },
    type = typeOf<T>(),
)

/**
 * Convenience function to create a lambda that doesn't require explicit type declarations
 *
 * @param block a scope for performing batch commands with an implicit type parameter
 */
suspend inline fun <reified T: Any> BatchCommandScope.withType(
    noinline block: suspend TypedBatchCommandScope<T>.() -> Unit,
) = withType(type = typeOf<T>(), block = block)
