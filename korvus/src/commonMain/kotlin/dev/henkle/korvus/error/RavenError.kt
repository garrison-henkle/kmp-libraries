package dev.henkle.korvus.error

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Represents an error thrown by RavenDB
 *
 * @property statusCode the response code of the RavenDB request
 * @property url the path of the request that threw the error
 * @property type the type of the error
 * @property msg the error message
 * @property stacktrace the internal RavenDB stacktrace for the error
 */
@Serializable
data class RavenError(
    @Transient
    val statusCode: Int = 500,
    @SerialName(value = "Url")
    val url: String,
    @SerialName(value = "Type")
    val type: String,
    @SerialName(value = "Message")
    val msg: String,
    @SerialName(value = "Error")
    val stacktrace: String,
) {
    override fun toString(): String = StringBuilder().apply {
        appendLine("RavenError(")
        append("\tstatusCode=")
        append(statusCode)
        appendLine(',')
        append("\turl=\"")
        append(url)
        appendLine("\",")
        append("\ttype=\"")
        append(type)
        appendLine("\",")
        append("\tmsg=\"")
        append(msg)
        appendLine("\",")
        appendLine(')')
    }.toString()
}