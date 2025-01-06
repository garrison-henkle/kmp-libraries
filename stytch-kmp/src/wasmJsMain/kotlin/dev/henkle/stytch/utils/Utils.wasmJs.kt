/*
 * IMPORTANT NOTE: This file must be kept in sync with Utils.js until at least Kotlin 2.1.0.
 * The js and wasmJs source sets cannot share code at the moment b/c of compiler restrictions. See:
 * https://youtrack.jetbrains.com/issue/KT-64214
 * https://youtrack.jetbrains.com/issue/KT-62398
 */

package dev.henkle.stytch.utils

import dev.henkle.stytch.model.sdk.Config
import kotlinx.browser.window

internal actual val platform: Platform = Platform.JSWasm

internal actual fun setLocation(url: String) {
    window.location.href = url
}

internal actual fun getScreenSize(): Pair<Int, Int> = window.screen.run { width to height }

internal actual fun getBrowserUserAgent(): String = window.navigator.userAgent
