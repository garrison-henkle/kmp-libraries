package dev.henkle.stytch

import dev.henkle.stytch.model.sdk.Config

actual fun prepareStytchClient(config: Config) {
    StytchClient.initInternal(platform = PlatformStytchClient(config = config))
}
