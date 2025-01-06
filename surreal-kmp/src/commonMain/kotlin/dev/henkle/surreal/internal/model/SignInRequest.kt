package dev.henkle.surreal.internal.model.functions

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class SignInRequest(
    @EncodeDefault(mode = EncodeDefault.Mode.NEVER)
    @SerialName(value = "NS")
    val namespace: String? = null,
    @EncodeDefault(mode = EncodeDefault.Mode.NEVER)
    @SerialName(value = "DB")
    val database: String? = null,
    @EncodeDefault(mode = EncodeDefault.Mode.NEVER)
    val user: String? = null,
    @EncodeDefault(mode = EncodeDefault.Mode.NEVER)
    @SerialName(value = "pass")
    val password: String? = null,
)
