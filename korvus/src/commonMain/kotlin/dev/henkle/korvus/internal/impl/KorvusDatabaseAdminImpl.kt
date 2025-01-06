package dev.henkle.korvus.internal.impl

import dev.henkle.korvus.KorvusConfig
import dev.henkle.korvus.KorvusDatabase
import dev.henkle.korvus.KorvusDatabaseAdmin
import dev.henkle.korvus.KorvusResult
import dev.henkle.korvus.error.KorvusError
import dev.henkle.korvus.internal.ext.parseFailure
import dev.henkle.korvus.internal.ext.url
import dev.henkle.korvus.internal.model.RavenDatabase
import dev.henkle.korvus.internal.model.request.DatabaseDeletionRequest
import dev.henkle.korvus.internal.model.response.RavenDatabaseResponse
import dev.henkle.korvus.internal.utils.client
import dev.henkle.korvus.withResult
import io.ktor.client.call.body
import io.ktor.client.request.parameter
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

class KorvusDatabaseAdminImpl(
    private val baseUrl: String,
    private val config: KorvusConfig,
) : KorvusDatabaseAdmin {
    override suspend fun get(
        name: String,
        replicationFactor: Int,
        createIfNeeded: Boolean,
    ): KorvusResult<KorvusDatabase> =
        if (createIfNeeded) {
            getNames().withResult(
                onSuccess = { names ->
                    if (name !in names) {
                        create(name = name, replicationFactor = replicationFactor)
                    } else {
                        null
                    }
                },
                onFailure = { error ->
                    KorvusResult.Failure(error = error)
                }
            )
        } else {
            null
        } ?: KorvusResult.Success(
            result = KorvusDatabaseImpl(
                name = name,
                replicationFactor = replicationFactor,
                baseUrl = baseUrl,
                dbAdmin = this@KorvusDatabaseAdminImpl,
                config = config,
            )
        )



    override suspend fun create(name: String, replicationFactor: Int): KorvusResult<KorvusDatabase> {
        val response = client.request {
            method = HttpMethod.Put
            url(baseUrl, "admin", "databases")
            parameter(key = "name", value = name)
            parameter(key = "replicationFactor", value = replicationFactor)
            contentType(type = ContentType.Application.Json)
            setBody(body = RavenDatabase(name = name))
        }

        return when (response.status) {
            HttpStatusCode.Created -> {
                KorvusResult.Success(
                    result = KorvusDatabaseImpl(
                        name = name,
                        replicationFactor = replicationFactor,
                        baseUrl = baseUrl,
                        dbAdmin = this@KorvusDatabaseAdminImpl,
                        config = config,
                    )
                )
            }
            else -> response.parseFailure()
        }
    }

    override suspend fun delete(vararg dbNames: String, hardDelete: Boolean): KorvusResult<Unit> {
        val response = client.request {
            method = HttpMethod.Delete
            url(baseUrl, "admin", "databases")
            contentType(type = ContentType.Application.Json)
            setBody(body = DatabaseDeletionRequest(dbNames = dbNames.toList(), hardDelete = hardDelete))
        }

        return when (response.status) {
            HttpStatusCode.OK -> KorvusResult.Success(Unit)
            // TODO(garrison): check if there is a no-op case under another status code when it doesn't delete anything
            else -> response.parseFailure()
        }
    }

    override suspend fun getNames(): KorvusResult<List<String>> {
        val response = client.request {
            method = HttpMethod.Get
            url(baseUrl, "databases")
        }

        return when (response.status) {
            HttpStatusCode.OK -> try {
                val result = response.body<RavenDatabaseResponse>()
                KorvusResult.Success(result = result.databases.map { it.name })
            } catch(ex: Exception) {
                KorvusResult.Failure(error = KorvusError.SDK(ex = ex))
            }
            // TODO(garrison): check if there is a no-op case under another status code when it doesn't get anything
            else -> response.parseFailure()
        }
    }
}