package dev.henkle.stytch.oauth

import co.touchlab.kermit.Logger
import dev.henkle.store.Storage
import dev.henkle.stytch.model.StytchAuthResponseData
import dev.henkle.stytch.model.StytchError
import dev.henkle.stytch.model.StytchResult
import dev.henkle.stytch.model.oauth.OAuthAppleStartParameters
import dev.henkle.stytch.model.oauth.request.OAuthAppleRequest
import dev.henkle.stytch.model.oauth.response.OAuthAppleResponse
import dev.henkle.stytch.model.sdk.Config
import dev.henkle.stytch.model.user.User
import dev.henkle.stytch.utils.Crypto
import dev.henkle.stytch.utils.StytchHTTPClient
import dev.henkle.stytch.utils.ext.ifSuccessfulAuth
import kotlinx.cinterop.BetaInteropApi
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import platform.AuthenticationServices.ASAuthorization
import platform.AuthenticationServices.ASAuthorizationAppleIDCredential
import platform.AuthenticationServices.ASAuthorizationAppleIDProvider
import platform.AuthenticationServices.ASAuthorizationController
import platform.AuthenticationServices.ASAuthorizationControllerDelegateProtocol
import platform.AuthenticationServices.ASAuthorizationControllerPresentationContextProvidingProtocol
import platform.AuthenticationServices.ASAuthorizationScopeEmail
import platform.AuthenticationServices.ASAuthorizationScopeFullName
import platform.Foundation.NSError
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.darwin.NSObject

typealias PresentationContextProvider =
    ASAuthorizationControllerPresentationContextProvidingProtocol

actual class GoogleOneTapProvider

@Suppress("UnnecessaryOptInAnnotation", "CAST_NEVER_SUCCEEDS")
@OptIn(BetaInteropApi::class)
actual class AppleOAuthProvider internal actual constructor(
    private val httpClient: StytchHTTPClient,
    storage: Storage,
    private val config: Config,
    private val onAuthenticate: (StytchAuthResponseData, User.Name?) -> Unit,
    launchBrowser: (url: String) -> Unit,
) {
    private val scope = CoroutineScope(Dispatchers.Main)

    actual suspend fun start(
        customScopes: List<String>?,
        loginRedirectUrl: String?,
        signupRedirectUrl: String?,
        iOSSessionDurationMin: UInt?
    ) {
        start(
            parameters = OAuthAppleStartParameters(
                customScopes = customScopes,
                loginRedirectUrl = loginRedirectUrl,
                signupRedirectUrl = signupRedirectUrl,
                iOSSessionDurationMin = iOSSessionDurationMin,
            ),
            presentationContextProvider = null,
        )
    }

    actual suspend fun start(parameters: OAuthAppleStartParameters) {
        start(
            parameters = parameters,
            presentationContextProvider = null,
        )
    }

    suspend fun start(
        customScopes: List<String>?,
        loginRedirectUrl: String?,
        signupRedirectUrl: String?,
        iOSSessionDurationMin: UInt?,
        presentationContextProvider: PresentationContextProvider? = null,
    ) {
        start(
            parameters = OAuthAppleStartParameters(
                customScopes = customScopes,
                loginRedirectUrl = loginRedirectUrl,
                signupRedirectUrl = signupRedirectUrl,
                iOSSessionDurationMin = iOSSessionDurationMin,
            ),
            presentationContextProvider = presentationContextProvider,
        )
    }

    suspend fun start(
        parameters: OAuthAppleStartParameters,
        presentationContextProvider: PresentationContextProvider? = null,
    ) {
        val (nonce, hashedNonce) = Crypto.generatePKCE(urlSafe = false)
        val provider = ASAuthorizationAppleIDProvider()
        val request = provider.createRequest()
        request.requestedScopes = listOf(ASAuthorizationScopeEmail, ASAuthorizationScopeFullName)
        request.nonce = hashedNonce

        val controller = ASAuthorizationController(authorizationRequests = listOf(request))
        controller.presentationContextProvider = presentationContextProvider
        val promise = CompletableDeferred<AppleOAuthResult>()
        controller.delegate = delegate(promise = promise)

        withContext(Dispatchers.Main) {
            controller.performRequests()
        }

        Logger.e("garrison") { "Performing Apple OAuth" }

        scope.launch {
            val result = promise.await()
            if (result is AppleOAuthResult.Success) {
                Logger.e("garrison") { "Apple OAuth succeeded" }
                authenticate(
                    idToken = result.idToken,
                    nonce = nonce,
                    sessionDurationMin = parameters.iOSSessionDurationMin,
                ).ifSuccessfulAuth { authResponseData ->
                    onAuthenticate(authResponseData, result.name)
                }
            } else {
                Logger.e("garrison") { "Apple OAuth failed: ${(result as AppleOAuthResult.Failure).ex}" }
            }
        }
    }

    private fun delegate(
        promise: CompletableDeferred<AppleOAuthResult>,
    ) = object : ASAuthorizationControllerDelegateProtocol, NSObject() {

        override fun authorizationController(
            controller: ASAuthorizationController,
            didCompleteWithAuthorization: ASAuthorization
        ) {
            Logger.e("garrison") { "successful apple oauth" }
            val credential = (didCompleteWithAuthorization.credential as? ASAuthorizationAppleIDCredential)
                ?: run {
                    promise.complete(
                        value = AppleOAuthResult.Failure(
                            ex = StytchError.Error(message = "Invalid ASAuthorizationCredential format."),
                        ),
                    )
                    return
                }
            Logger.e("garrison") { "got creds" }

            val idToken = credential.identityToken?.let {
                NSString.create(data = it, encoding = NSUTF8StringEncoding) as String
            } ?: run {
                promise.complete(
                    value = AppleOAuthResult.Failure(
                        ex = StytchError.Error(message = "Credentials are missing an identity token!"),
                    ),
                )
                return
            }
            Logger.e("garrison") { "got token" }

            val name = if (credential.fullName?.givenName != null || credential.fullName?.familyName != null) {
                User.Name(
                    firstName = credential.fullName?.givenName,
                    lastName = credential.fullName?.familyName,
                )
            } else {
                null
            }
            Logger.e("garrison") { "got name? $name" }

            promise.complete(value = AppleOAuthResult.Success(idToken = idToken, name = name))
        }

        override fun authorizationController(
            controller: ASAuthorizationController,
            didCompleteWithError: NSError
        ) {
            Logger.e("garrison") { "failed apple oauth" }
            promise.complete(
                value = AppleOAuthResult.Failure(
                    ex = StytchError.Error(
                        message = "ASAuthorizationController authorization failed.",
                        ex = Exception(didCompleteWithError.localizedDescription),
                    ),
                )
            )
        }
    }

    private suspend fun authenticate(
        idToken: String,
        nonce: String,
        sessionDurationMin: UInt? = null,
    ): StytchResult<OAuthAppleResponse> =
        httpClient.post<OAuthAppleRequest, OAuthAppleResponse>(
            path = PATH_OAUTH_AUTHENTICATE_APPLE,
            body = OAuthAppleRequest(
                idToken = idToken,
                nonce = nonce,
                sessionDurationMin = sessionDurationMin ?: config.sessionDurationMin,
            ),
        )

    private sealed interface AppleOAuthResult {
        data class Success(
            val idToken: String,
            val name: User.Name?,
        ) : AppleOAuthResult

        data class Failure(val ex: StytchError.Error) : AppleOAuthResult
    }

    companion object {
        private const val PATH_OAUTH_AUTHENTICATE_APPLE = "oauth/apple/id_token/authenticate"
    }
}
