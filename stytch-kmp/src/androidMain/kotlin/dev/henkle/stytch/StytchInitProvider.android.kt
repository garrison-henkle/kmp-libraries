package dev.henkle.stytch

import dev.henkle.context.ContextProvider
import dev.henkle.stytch.model.sdk.Config
import java.lang.ref.WeakReference

actual fun prepareStytchClient(config: Config) {
    val context = ContextProvider.context?.let { WeakReference(it) }
        ?: throw IllegalStateException("Unable to prepare StytchClient: null context reference")
    StytchClient.initInternal(platform = PlatformStytchClient(config = config, context = context))
}
