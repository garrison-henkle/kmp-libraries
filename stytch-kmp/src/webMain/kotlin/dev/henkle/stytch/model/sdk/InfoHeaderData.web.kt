package dev.henkle.stytch.model.sdk

import com.benasher44.uuid.uuid4
import dev.henkle.stytch.BuildKonfig
import dev.henkle.stytch.utils.platform
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class InfoHeaderData(
    val app: App,
    @SerialName(value = "event_id")
    val eventId: String = "$EVENT_ID_PREFIX${uuid4()}",
    @SerialName(value = "app_session_id")
    val appSessionId: String = "$APP_SESSION_ID_PREFIX${uuid4()}",
    @SerialName(value = "persistent_id")
    val persistentId: String = "$PERSISTENT_ID_PREFIX${uuid4()}",
    @SerialName(value = "client_sent_at")
    val clientSentAt: String = Clock.System.now().toString(),
    val timezone: String = TimeZone.currentSystemDefault().id,
    val sdk: SDK = SDK(),
) {
    constructor(appIdentifier: String) : this(app = App(identifier = appIdentifier))

    @Serializable
    data class App(val identifier: String)

    @Serializable
    data class SDK(
        val identifier: String = SDK_NAME,
        val version: String = BuildKonfig.versionName,
    )

    companion object {
        private val SDK_NAME = "StytchKMP-${platform.name} Unofficial SDK"

        private const val EVENT_ID_PREFIX = "event-id-"
        private const val APP_SESSION_ID_PREFIX = "app-session-id-"
        private const val PERSISTENT_ID_PREFIX = "persistent-id-"
    }
}
