package dev.henkle.stytch.demo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import dev.henkle.stytch.StytchClient
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(onClientReady: (client: StytchClient) -> Unit = {}) {
    val scope = rememberCoroutineScope()
    val client = remember {
        StytchClient.init(publicToken = BuildKonfig.stytchPublicToken).also(onClientReady)
    }

    var phoneNumber by remember { mutableStateOf("+14175296773") }
    var otp by remember { mutableStateOf("") }
    var otpMethod by remember { mutableStateOf<String?>(null) }
    val session by client.sessionFlow.collectAsState(initial = null)
    val user by client.userFlow.collectAsState(initial = null)

    Column {
        Text(text = "Phone #:")
        TextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
        )
        Spacer(modifier = Modifier.height(height = 10.dp))
        Text(text = "OTP:")
        TextField(
            value = otp,
            onValueChange = { otp = it },
        )
        Spacer(modifier = Modifier.height(height = 10.dp))
        Row {
            Button(
                onClick = {
                    scope.launch {
                        client.otp.sms.loginOrCreate(phoneNumber = phoneNumber).withResult(
                            onSuccess = { otpMethod = it.methodId },
                            onFailure = { Logger.e("garrison") { it.toString() } }
                        )
                    }
                }
            ) {
                Text(text = "Send OTP")
            }

            Button(
                onClick = {
                    otpMethod?.also { method ->
                        scope.launch {
                            client.otp.authenticate(methodId = method, code = otp).withResult(
                                onSuccess = {
                                    Logger.w("garrison") {
                                        "Authenticated: ${it.user} (jwt=${it.sessionJwt})"
                                    }
                                },
                                onFailure = { Logger.e("garrison") { it.toString() } }
                            )
                        }
                    }

                }
            ) {
                Text(text = "Submit OTP")
            }

            Button(
                onClick = {
                    scope.launch {
                        client.oauth.google.start()
                    }
                }
            ) {
                Text(text = "Google OAuth")
            }
        }
        Spacer(modifier = Modifier.height(height = 10.dp))
        Row {
            Button(
                onClick = {
                    scope.launch {
                        client.oauth.apple.start()
                    }
                }
            ) {
                Text(text = "Apple OAuth")
            }

            Button(
                onClick = {
                    scope.launch {
                        client.sessions.revoke()
                    }
                }
            ) {
                Text(text = "Clear")
            }
        }
        Spacer(modifier = Modifier.height(height = 10.dp))
        Text(text = "User:\n$user\n\nSession:\n$session")
    }
}
