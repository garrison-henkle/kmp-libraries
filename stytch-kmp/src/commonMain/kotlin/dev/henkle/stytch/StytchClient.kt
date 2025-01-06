package dev.henkle.stytch

import co.touchlab.kermit.Logger
import dev.henkle.store.KeyMP
import dev.henkle.stytch.model.StytchAuthResponseData
import dev.henkle.stytch.model.StytchError
import dev.henkle.stytch.model.sdk.AuthenticationData
import dev.henkle.stytch.model.sdk.Config
import dev.henkle.stytch.model.sdk.Config.Companion.DEFAULT_CALLBACK_HOST
import dev.henkle.stytch.model.sdk.Config.Companion.DEFAULT_CALLBACK_SCHEME
import dev.henkle.stytch.model.sdk.Config.Companion.DEFAULT_LOGIN_LINK_EXPIRATION_MIN
import dev.henkle.stytch.model.sdk.Config.Companion.DEFAULT_OAUTH_TIMEOUT_MIN
import dev.henkle.stytch.model.sdk.Config.Companion.DEFAULT_OTP_EXPIRATION_MIN
import dev.henkle.stytch.model.sdk.Config.Companion.DEFAULT_RESET_LINK_EXPIRATION_MIN
import dev.henkle.stytch.model.sdk.Config.Companion.DEFAULT_SESSION_DURATION_MIN
import dev.henkle.stytch.model.sdk.Config.Companion.DEFAULT_SIGNUP_LINK_EXPIRATION_MIN
import dev.henkle.stytch.model.session.Session
import dev.henkle.stytch.model.user.User
import dev.henkle.stytch.oauth.OAuth
import dev.henkle.stytch.oauth.OAuthImpl
import dev.henkle.stytch.otp.OTP
import dev.henkle.stytch.otp.OTPImpl
import dev.henkle.stytch.sessions.SessionRepository
import dev.henkle.stytch.sessions.Sessions
import dev.henkle.stytch.sessions.SessionsImpl
import dev.henkle.stytch.utils.IODispatcher
import dev.henkle.stytch.utils.StytchHTTPClient
import dev.henkle.stytch.utils.createKtorClient
import dev.henkle.stytch.utils.getDefaultRedirectUrl
import dev.henkle.stytch.utils.launchBrowser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Suppress("MemberVisibilityCanBePrivate")
class StytchClient private constructor(val platform: PlatformStytchClient) {
    private lateinit var config: Config
    private var configured = false

    private val scope = CoroutineScope(IODispatcher)
    private val storage by lazy { KeyMP.secureStorage }
    private val sessionRepo by lazy { SessionRepository(storage = storage) }
    private val httpClient by lazy {
        createKtorClient(
            publicToken = config.publicToken,
            platform = platform,
            sessions = sessionRepo,
        )
    }
    private val client by lazy {
        StytchHTTPClient(httpClient = httpClient, onUnauthorized = ::onUnauthorized)
    }

    private val authenticationData = MutableStateFlow<AuthenticationData?>(null)

    val user: User? get() = authenticationData.value?.user
    val userFlow: StateFlow<User?> = authenticationData.map { it?.user }.stateIn(
        scope = scope,
        started = SharingStarted.Lazily,
        initialValue = user,
    )

    val sessionToken: String? get() = authenticationData.value?.token
    val sessionTokenFlow: StateFlow<String?> = authenticationData.map { it?.token }.stateIn(
        scope = scope,
        started = SharingStarted.Lazily,
        initialValue = sessionToken,
    )

    val sessionJwt: String? get() = authenticationData.value?.jwt
    val sessionJwtFlow: StateFlow<String?> = authenticationData.map { it?.jwt }.stateIn(
        scope = scope,
        started = SharingStarted.Lazily,
        initialValue = sessionJwt,
    )

    val session: Session? get() = authenticationData.value?.session
    val sessionFlow: StateFlow<Session?> = authenticationData.map { it?.session }.stateIn(
        scope = scope,
        started = SharingStarted.Lazily,
        initialValue = session,
    )

    val otp: OTP by lazy {
        OTPImpl(client = client, config = config, onAuthenticate = ::onAuthenticate)
    }

    val sessions: Sessions by lazy {
        SessionsImpl(
            client = client,
            config = config,
            onAuthenticate = ::onAuthenticate,
            onRevoke = ::onUnauthorized,
        )
    }

    val oauth: OAuth by lazy {
        OAuthImpl(
            client = client,
            storage = storage,
            config = config,
            onAuthenticate = { authResponse, name ->
                onAuthenticate(response = authResponse)
                authenticationData.update {
                    it?.let { authData -> authData.copy(user = authData.user.copy(name = name)) }
                }
            },
            launchBrowser = { url ->
                launchBrowser(
                    url = url,
                    defaultCallbackScheme = config.callbackScheme,
                    oauthTimeoutMin = config.oauthTimeoutMin,
                    platformStytchClient = platform,
                    callback = { uri -> handleUri(uri = uri){ Logger.e("garrison") { it.toString() } } },
                )
            },
        )
    }

    fun configure(
        publicToken: String,
        callbackScheme: String = DEFAULT_CALLBACK_SCHEME,
        callbackHost: String = DEFAULT_CALLBACK_HOST,
        sessionDurationMin: UInt? = DEFAULT_SESSION_DURATION_MIN,
        oauthTimeoutMin: UInt = DEFAULT_OAUTH_TIMEOUT_MIN,
        loginRedirectUrl: String? = getDefaultRedirectUrl(),
        signupRedirectUrl: String? = getDefaultRedirectUrl(),
        resetPasswordRedirectUrl: String? = getDefaultRedirectUrl(),
        loginExpirationMinutes: UInt = DEFAULT_LOGIN_LINK_EXPIRATION_MIN,
        signupExpirationMinutes: UInt = DEFAULT_SIGNUP_LINK_EXPIRATION_MIN,
        resetPasswordExpirationMinutes: UInt = DEFAULT_RESET_LINK_EXPIRATION_MIN,
        otpExpirationMinutes: UInt = DEFAULT_OTP_EXPIRATION_MIN,
    ): StytchClient = configure(
        config = Config(
            publicToken = publicToken,
            callbackScheme = callbackScheme,
            callbackHost = callbackHost,
            sessionDurationMin = sessionDurationMin,
            oauthTimeoutMin = oauthTimeoutMin,
            loginRedirectUrl = loginRedirectUrl,
            signupRedirectUrl = signupRedirectUrl,
            resetPasswordRedirectUrl = resetPasswordRedirectUrl,
            loginExpirationMinutes = loginExpirationMinutes,
            signupExpirationMinutes = signupExpirationMinutes,
            resetPasswordExpirationMinutes = resetPasswordExpirationMinutes,
            otpExpirationMinutes = otpExpirationMinutes,
        ),
    )

    internal fun configure(config: Config): StytchClient {
        if (configured) {
            throw IllegalStateException("StytchClient cannot be configured more than once!")
        }
        this.config = config
        configured = true
        return this
    }

    private fun onAuthenticate(response: StytchAuthResponseData) {
        authenticationData.value = AuthenticationData(
            token = response.sessionToken,
            jwt = response.sessionJwt,
            user = response.user,
            session = response.session,
        )
    }

    private fun onUnauthorized() {
        authenticationData.value = null
        sessionRepo.clear()
    }

    private fun isHandledUri(uri: String): UriType? = platform.isHandledUri(uri = uri)

    fun handleUri(uri: String, onFinished: (result: UriHandlingResult) -> Unit) {
        val type = isHandledUri(uri)
        if (type != null) {
            scope.launch {
                onFinished(handleUriInternal(uri = uri, type = type))
            }
        } else {
            onFinished(UriHandlingResult.Ignored)
        }
    }

    suspend fun handleUri(uri: String): UriHandlingResult = withContext(IODispatcher) {
        val type = isHandledUri(uri)
        if (type != null) {
            handleUriInternal(uri = uri, type = type)
        } else {
            UriHandlingResult.Ignored
        }
    }

    private suspend fun handleUriInternal(uri: String, type: UriType): UriHandlingResult =
        when (val parsed = platform.handleUri(uri = uri, type = type)) {
            is UriType.OAuth ->
                oauth.authenticate(token = parsed.token)
                    .withResult(
                        onSuccess = { UriHandlingResult.SDKHandled },
                        onFailure = { UriHandlingResult.SDKHandledWithError(error = it) },
                    )

            null -> UriHandlingResult.SDKHandledWithError(
                error = StytchError.Error(
                    message = "Uri is of known type but could not be parsed: $uri",
                ),
            )
        }

    sealed interface UriHandlingResult {
        data object SDKHandled : UriHandlingResult
        data class SDKHandledWithError(val error: StytchError) : UriHandlingResult
        data object Ignored : UriHandlingResult
    }

    internal sealed interface UriParsingResult
    internal sealed interface UriType {
        data class OAuth(val token: String) : UriParsingResult {
            companion object : UriType
        }
    }

    companion object {
        internal fun initInternal(platform: PlatformStytchClient) {
            if (this::instance.isInitialized) {
                throw IllegalStateException("StytchClient has already been initialized")
            }
            instance = StytchClient(platform = platform)
        }

        lateinit var instance: StytchClient

        fun init(
            publicToken: String,
            callbackScheme: String = DEFAULT_CALLBACK_SCHEME,
            callbackHost: String = DEFAULT_CALLBACK_HOST,
            sessionDurationMin: UInt? = DEFAULT_SESSION_DURATION_MIN,
            oauthTimeoutMin: UInt = DEFAULT_OAUTH_TIMEOUT_MIN,
            loginRedirectUrl: String? = getDefaultRedirectUrl(),
            signupRedirectUrl: String? = getDefaultRedirectUrl(),
            resetPasswordRedirectUrl: String? = getDefaultRedirectUrl(),
            loginExpirationMinutes: UInt = DEFAULT_LOGIN_LINK_EXPIRATION_MIN,
            signupExpirationMinutes: UInt = DEFAULT_SIGNUP_LINK_EXPIRATION_MIN,
            resetPasswordExpirationMinutes: UInt = DEFAULT_RESET_LINK_EXPIRATION_MIN,
            otpExpirationMinutes: UInt = DEFAULT_OTP_EXPIRATION_MIN,
        ): StytchClient {
            val config = Config(
                publicToken = publicToken,
                callbackScheme = callbackScheme,
                callbackHost = callbackHost,
                sessionDurationMin = sessionDurationMin,
                oauthTimeoutMin = oauthTimeoutMin,
                loginRedirectUrl = loginRedirectUrl,
                signupRedirectUrl = signupRedirectUrl,
                resetPasswordRedirectUrl = resetPasswordRedirectUrl,
                loginExpirationMinutes = loginExpirationMinutes,
                signupExpirationMinutes = signupExpirationMinutes,
                resetPasswordExpirationMinutes = resetPasswordExpirationMinutes,
                otpExpirationMinutes = otpExpirationMinutes,
            )
            prepareStytchClient(config = config)
            return instance.configure(config = config)
        }
    }
}
