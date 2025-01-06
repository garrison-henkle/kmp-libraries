package dev.henkle.stytch

import dev.henkle.stytch.model.sdk.Config

/**
 * Calls the internal initializers for the StytchClient.
 *
 * No-op on Android, as it is initialized by a ContentProvider.
 */
internal expect fun prepareStytchClient(config: Config)
