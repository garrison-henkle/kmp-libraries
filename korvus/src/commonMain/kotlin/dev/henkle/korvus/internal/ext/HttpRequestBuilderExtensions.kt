package dev.henkle.korvus.internal.ext

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.url

fun HttpRequestBuilder.url(baseUrl: String, vararg path: String) =
    url(urlString = path.joinToString(prefix = "$baseUrl/", separator = "/"))
