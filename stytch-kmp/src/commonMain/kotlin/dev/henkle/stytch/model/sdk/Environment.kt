package dev.henkle.stytch.model.sdk

import dev.henkle.stytch.utils.ext.isTestToken

enum class Environment(val apiUrl: String) {
    Live(apiUrl = "https://api.stytch.com/v1"),
    Test(apiUrl = "https://test.stytch.com/v1"),
    ;

    companion object {
        fun fromToken(publicToken: String): Environment =
            if (publicToken.isTestToken) Test else Live
    }
}
