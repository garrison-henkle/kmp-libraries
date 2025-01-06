package dev.henkle.stytch

import android.content.Context
import androidx.activity.result.ActivityResultRegistry
import dev.henkle.stytch.StytchClient.UriParsingResult
import dev.henkle.stytch.StytchClient.UriType
import dev.henkle.stytch.model.sdk.Config
import dev.henkle.stytch.utils.isOAuthCallback
import java.lang.ref.WeakReference

actual class PlatformStytchClient internal constructor(
    private val config: Config,
    internal val context: WeakReference<Context>,
){
    // Google did deprecate startActivityForResult, so we're forcing users to add androidx.activity
    private var _registry: WeakReference<ActivityResultRegistry>? = null
    internal val registry: ActivityResultRegistry? get() = _registry?.get()
    internal var overrideOAuthCallbackScheme: String? = null
    internal var overrideOAuthCallbackHost: String? = null

    internal actual fun isHandledUri(uri: String): UriType? =
        when {
            uri.isOAuthCallback(
                config = config,
                overrideOAuthCallbackScheme = overrideOAuthCallbackScheme,
                overrideOAuthCallbackHost = overrideOAuthCallbackHost,
            ) -> UriType.OAuth

            else -> null
        }


    internal actual suspend fun handleUri(uri: String, type: UriType): UriParsingResult? =
        when(type) {
            UriType.OAuth -> parseOAuthToken(uri = uri)
        }

    fun configure(
        activityResultRegistry: ActivityResultRegistry,
        callbackScheme: String = Config.DEFAULT_CALLBACK_SCHEME,
        callbackHost: String = Config.DEFAULT_CALLBACK_HOST,
    ) {
        this.overrideOAuthCallbackScheme = callbackScheme
        this.overrideOAuthCallbackHost = callbackHost
        this._registry = WeakReference(activityResultRegistry)
    }
}
