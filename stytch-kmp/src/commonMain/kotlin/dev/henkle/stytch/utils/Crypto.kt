package dev.henkle.stytch.utils

import dev.henkle.stytch.model.sdk.Config
import io.ktor.utils.io.core.toByteArray
import org.kotlincrypto.SecureRandom
import org.kotlincrypto.hash.sha2.SHA256
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalStdlibApi::class)
object Crypto {
    private val random by lazy { SecureRandom() }
    private val hash by lazy { SHA256() }

    data class PKCE(val codeVerifier: String, val codeChallenge: String)

    @OptIn(ExperimentalEncodingApi::class)
    fun generatePKCE(urlSafe: Boolean = true): PKCE {
        val codeVerifierBytes = random.nextBytesOf(count = Config.CODE_VERIFIER_BYTE_COUNT)
        val codeVerifier = codeVerifierBytes.toHexString()
        val hashedCodeVerifier = hash.digest(input = codeVerifier.toByteArray())
        val codeChallenge = if(urlSafe) {
            Base64.UrlSafe.encode(source = hashedCodeVerifier).trimEnd('=')
        } else {
            Base64.encode(source = hashedCodeVerifier)
        }
        return PKCE(
            codeVerifier = codeVerifier,
            codeChallenge = codeChallenge,
        )
    }
}
