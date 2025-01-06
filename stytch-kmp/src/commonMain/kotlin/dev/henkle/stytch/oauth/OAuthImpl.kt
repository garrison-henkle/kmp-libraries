package dev.henkle.stytch.oauth

import dev.henkle.store.Storage
import dev.henkle.stytch.model.StytchAuthResponseData
import dev.henkle.stytch.model.StytchError
import dev.henkle.stytch.model.StytchResult
import dev.henkle.stytch.model.oauth.OAuthAuthenticateParameters
import dev.henkle.stytch.model.oauth.OAuthStartParameters
import dev.henkle.stytch.model.oauth.request.OAuthAuthenticateRequest
import dev.henkle.stytch.model.oauth.request.OAuthStartRequest
import dev.henkle.stytch.model.oauth.response.OAuthResponse
import dev.henkle.stytch.model.sdk.Config
import dev.henkle.stytch.model.session.SessionKey
import dev.henkle.stytch.model.user.User
import dev.henkle.stytch.utils.Crypto
import dev.henkle.stytch.utils.IODispatcher
import dev.henkle.stytch.utils.StytchHTTPClient
import dev.henkle.stytch.utils.ext.ifSuccessfulAuth
import kotlinx.coroutines.withContext

internal class OAuthImpl(
    private val client: StytchHTTPClient,
    private val storage: Storage,
    private val config: Config,
    private val onAuthenticate: (StytchAuthResponseData, User.Name?) -> Unit,
    launchBrowser: (url: String) -> Unit,
) : OAuth {
    private val factory = OAuthProviderFactory(
        storage = storage,
        config = config,
        launchBrowser = launchBrowser,
    )

    override val amazon: OAuth.Provider = factory.create(provider = OAuthProvider.Amazon)
    override val apple: AppleOAuthProvider = AppleOAuthProvider(
        storage = storage,
        config = config,
        launchBrowser = launchBrowser,
        httpClient = client,
        onAuthenticate = onAuthenticate,
    )
    override val bitbucket: OAuth.Provider = factory.create(provider = OAuthProvider.Bitbucket)
    override val coinbase: OAuth.Provider = factory.create(provider = OAuthProvider.Coinbase)
    override val discord: OAuth.Provider = factory.create(provider = OAuthProvider.Discord)
    override val facebook: OAuth.Provider = factory.create(provider = OAuthProvider.Facebook)
    override val figma: OAuth.Provider = factory.create(provider = OAuthProvider.Figma)
    override val github: OAuth.Provider = factory.create(provider = OAuthProvider.GitHub)
    override val gitlab: OAuth.Provider = factory.create(provider = OAuthProvider.GitLab)
    override val google: OAuth.Provider = factory.create(provider = OAuthProvider.Google)
    override val googleOneTap: GoogleOneTapProvider = GoogleOneTapProvider()
    override val linkedin: OAuth.Provider = factory.create(provider = OAuthProvider.LinkedIn)
    override val microsoft: OAuth.Provider = factory.create(provider = OAuthProvider.Microsoft)
    override val salesforce: OAuth.Provider = factory.create(provider = OAuthProvider.Salesforce)
    override val slack: OAuth.Provider = factory.create(provider = OAuthProvider.Slack)
    override val snapchat: OAuth.Provider = factory.create(provider = OAuthProvider.Snapchat)
    override val spotify: OAuth.Provider = factory.create(provider = OAuthProvider.Spotify)
    override val tiktok: OAuth.Provider = factory.create(provider = OAuthProvider.TikTok)
    override val twitch: OAuth.Provider = factory.create(provider = OAuthProvider.Twitch)
    override val twitter: OAuth.Provider = factory.create(provider = OAuthProvider.Twitter)
    override val yahoo: OAuth.Provider = factory.create(provider = OAuthProvider.Yahoo)

    override suspend fun authenticate(
        token: String,
        sessionDurationMin: UInt?
    ): StytchResult<OAuthResponse> = authenticate(
        parameters = OAuthAuthenticateParameters(
            token = token,
            sessionDurationMin = sessionDurationMin,
        )
    )

    override suspend fun authenticate(
        parameters: OAuthAuthenticateParameters
    ): StytchResult<OAuthResponse> {
        val verifier = storage[SessionKey.PKCE.id] ?: return StytchResult.Failure(
            error = StytchError.Error(
                message = "The PKCE code challenge was missing from storage!",
            )
        )
        return client.post<OAuthAuthenticateRequest, OAuthResponse>(
            path = PATH_OAUTH_AUTHENTICATE,
            body = OAuthAuthenticateRequest(
                token = parameters.token,
                codeVerifier = verifier,
                sessionDurationMin = parameters.sessionDurationMin ?: config.sessionDurationMin,
            ),
        ).ifSuccessfulAuth(perform = { onAuthenticate(it, null) })
    }

    private class OAuthProviderFactory(
        private val storage: Storage,
        private val config: Config,
        private val launchBrowser: (url: String) -> Unit,
    ) {
        fun create(provider: OAuthProvider): OAuth.Provider =
            OAuthProviderImpl(
                provider = provider,
                storage = storage,
                config = config,
                launchBrowser = launchBrowser,
            )
    }

    class OAuthProviderImpl(
        override val provider: OAuthProvider,
        private val storage: Storage,
        private val config: Config,
        private val launchBrowser: (url: String) -> Unit,
    ) : OAuth.Provider {
        override suspend fun start(
            customScopes: List<String>?,
            loginRedirectUrl: String?,
            signupRedirectUrl: String?,
        ) = start(
            parameters = OAuthStartParameters(
                publicToken = config.publicToken,
                loginRedirectUrl = loginRedirectUrl ?: config.loginRedirectUrl,
                signupRedirectUrl = signupRedirectUrl ?: config.signupRedirectUrl,
                customScopes = customScopes,
            ),
        )

        override suspend fun start(parameters: OAuthStartParameters) {
            val codeChallenge = withContext(IODispatcher) {
                Crypto.generatePKCE().also { (codeVerifier) ->
                    storage[SessionKey.PKCE.id] = codeVerifier
                }.codeChallenge
            }

            val queryString = OAuthStartRequest(
                codeChallenge = codeChallenge,
                publicToken = config.publicToken,
                loginRedirectUrl = parameters.loginRedirectUrl ?: config.loginRedirectUrl,
                signupRedirectUrl = parameters.signupRedirectUrl ?: config.signupRedirectUrl,
                customScopes = parameters.customScopes,
            ).queryString

            val url = StringBuilder().apply {
                append(config.env.apiUrl)
                append('/')
                append(PATH_OAUTH_START_1)
                append('/')
                append(provider.lowercaseName)
                append('/')
                append(PATH_OAUTH_START_2)
                append('?')
                append(queryString)
            }.toString()

            launchBrowser(url)
        }

        companion object {
            private const val PATH_OAUTH_START_1 = "public/oauth"
            private const val PATH_OAUTH_START_2 = "start"
        }
    }

    companion object {
        private const val PATH_OAUTH_AUTHENTICATE = "oauth/authenticate"
    }
}
