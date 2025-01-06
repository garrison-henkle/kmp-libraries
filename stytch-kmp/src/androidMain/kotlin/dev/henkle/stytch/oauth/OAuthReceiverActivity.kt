package dev.henkle.stytch.oauth

import android.app.Activity
import android.os.Bundle
import co.touchlab.kermit.Logger

internal class OAuthReceiverActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.e("garrison") { "OAuthReceiverActivity: onCreate: ${intent.data}" }
        val oauthActivityIntent = OAuthActivity.createResponseHandlingIntent(
            context = this,
            responseUri = intent.data,
        )
        startActivity(oauthActivityIntent)
        finish()
    }
}
