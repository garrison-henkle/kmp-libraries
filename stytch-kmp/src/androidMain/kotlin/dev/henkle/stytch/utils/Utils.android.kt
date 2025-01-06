package dev.henkle.stytch.utils

import android.os.Build
import android.webkit.WebSettings
import androidx.activity.result.contract.ActivityResultContracts
import co.touchlab.kermit.Logger
import dev.henkle.stytch.PlatformStytchClient
import dev.henkle.stytch.StytchClient
import dev.henkle.stytch.model.sdk.InfoHeaderData
import dev.henkle.stytch.oauth.OAuthActivity

private const val OS_NAME = "Android"
private const val KEY_LAUNCH_BROWSER = "launchBrowserKey"

internal actual val platform: Platform = Platform.Android

internal actual fun getInfoHeaderData(platform: PlatformStytchClient): InfoHeaderData {
    val context = platform.context.get()
    var appPackage = ""
    var appVersion = ""
    var screenSize = ""
    context?.also { ctx ->
        ctx.packageName?.also { pkgName ->
            appPackage = pkgName
            ctx.packageManager.getPackageInfo(pkgName, 0).versionName?.also { appVersion = it }
            val width = ctx.resources.displayMetrics.widthPixels
            val height = ctx.resources.displayMetrics.heightPixels
            screenSize = "($width,$height)"
        }
    }
    return InfoHeaderData(
        appPackage = appPackage,
        appVersion = appVersion,
        osName = OS_NAME,
        osVersion = Build.VERSION.SDK_INT.toString(),
        deviceModel = Build.MODEL ?: "",
        deviceScreenSize = screenSize,
    )
}

internal actual fun getUserAgent(): String {
    val context = StytchClient.instance.platform.context.get()
    return context?.let { WebSettings.getDefaultUserAgent(it) } ?: ""
}

internal actual fun launchBrowser(
    url: String,
    defaultCallbackScheme: String,
    oauthTimeoutMin: UInt,
    platformStytchClient: PlatformStytchClient,
    callback: (uri: String) -> Unit,
) {
    Logger.e("garrison") { "Launching browser" }
    StytchClient.instance.platform.registry?.let { registry ->
        Logger.e("garrison") { "Got registry" }
        StytchClient.instance.platform.context.get()?.also { context ->
            Logger.e("garrison") { "got context" }
            val oauthActivityIntent = OAuthActivity.createUriLaunchingIntent(
                context = context,
                uri = url,
            )
            registry.register(KEY_LAUNCH_BROWSER, ActivityResultContracts.StartActivityForResult()) {
                Logger.e("garrison") { "got activity result" }
                it.data?.data?.also { uri ->
                    Logger.e("garrison") { "result contained uri: $uri" }
                    callback(uri.toString())
                }
            }.launch(oauthActivityIntent)
            Logger.e("garrison") { "launched the OAuthActivity launcher" }
        }
    } ?: Logger.e("StytchKMP") {
        "Unable to launch a browser to start the OAuth flow! " +
            "Please set an ActivityResultRegistry at StytchClient.platform.activityResultRegistry!"
    }
}
