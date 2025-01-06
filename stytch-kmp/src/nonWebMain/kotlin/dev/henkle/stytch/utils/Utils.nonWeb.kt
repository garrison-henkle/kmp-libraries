package dev.henkle.stytch.utils

import dev.henkle.stytch.PlatformStytchClient
import dev.henkle.stytch.model.sdk.InfoHeaderData

internal expect fun getInfoHeaderData(platform: PlatformStytchClient): InfoHeaderData
