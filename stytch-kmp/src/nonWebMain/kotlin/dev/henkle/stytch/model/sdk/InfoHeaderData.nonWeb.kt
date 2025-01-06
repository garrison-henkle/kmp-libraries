package dev.henkle.stytch.model.sdk

import dev.henkle.stytch.BuildKonfig

internal class InfoHeaderData(
    val appPackage: String,
    val appVersion: String,
    val osName: String,
    val osVersion: String,
    val deviceModel: String,
    val deviceScreenSize: String,
) {
    val json: String = """
        {
          "sdk": { "$ID": "$SDK_NAME", "$VER": "${BuildKonfig.versionName}" },
          "app": { "$ID": "$appPackage", "$VER": "$appVersion" },
          "os": { "$ID": "$osName", "$VER": "$osVersion" },
          "device": { "model": "$deviceModel", "screen_size": "$deviceScreenSize" }
        }
        """.trimIndent()

    companion object {
        private const val SDK_NAME = "stytch-kmp-unofficial"

        private const val ID = "identifier"
        private const val VER = "version"
    }
}
