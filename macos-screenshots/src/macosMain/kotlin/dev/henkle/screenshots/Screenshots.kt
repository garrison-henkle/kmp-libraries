@file:OptIn(ExperimentalForeignApi::class)

package dev.henkle.screenshots

import dev.henkle.screenshots.jni.JNIEnvVar
import dev.henkle.screenshots.jni.jclass
import dev.henkle.screenshots.jni.jstring
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.invoke
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.toKString
import kotlinx.cinterop.wcstr
import platform.CoreFoundation.CFTypeRef
import platform.CoreFoundation.CFURLRef
import platform.CoreGraphics.CGDisplayCreateImage
import platform.CoreGraphics.CGMainDisplayID
import platform.CoreServices.kUTTypePNG
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSURL
import platform.ImageIO.CGImageDestinationAddImage
import platform.ImageIO.CGImageDestinationCreateWithURL
import platform.ImageIO.CGImageDestinationFinalize
import kotlin.experimental.ExperimentalNativeApi

/* Native Code */

@Suppress("UNCHECKED_CAST")
@OptIn(ExperimentalForeignApi::class)
fun takeScreenshot(destinationUrl: String) {
    val imageRef = CGDisplayCreateImage(displayID = CGMainDisplayID())
    NSURL.URLWithString(URLString = destinationUrl)?.useCFBridging {
        val cfUrl = it as CFURLRef
        CGImageDestinationCreateWithURL(
            url = cfUrl,
            type = kUTTypePNG,
            count = 1u,
            options = null,
        )?.also { cfImageDestUrlRef ->
            CGImageDestinationAddImage(idst = cfImageDestUrlRef, image = imageRef, properties = null)
            CGImageDestinationFinalize(idst = cfImageDestUrlRef)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun Any.useCFBridging(block: (CFTypeRef) -> Unit) {
    CFBridgingRetain(X = this)?.also { ref ->
        try {
            block(ref)
        } finally {
            CFBridgingRelease(X = ref)
        }
    }
}

/* Java Native Interface */

typealias JNIEnvParam = CPointer<JNIEnvVar>

val JNIEnvParam.nativeInterface get() = pointed.pointed ?: error("JNI JNINativeInterface missing!")

@Suppress("unused")
@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("Java_dev_henkle_screenshots_MainKt_takeScreenshot")
fun takeScreenshotJni(env: JNIEnvParam, clazz: jclass, destinationUrl: jstring) {
    val url = destinationUrl.toKString(env = env)
    takeScreenshot(destinationUrl = url)
}

private fun String.toJString(env: JNIEnvParam): jstring = memScoped {
    val newString = env.nativeInterface.NewString ?: error("JNI NewString missing!")
    newString(env, wcstr.ptr, this@toJString.length)!!
}

private fun jstring.toKString(env: JNIEnvParam): String {
    val getStringChars = env.nativeInterface.GetStringChars ?: error("JNI GetStringChars missing!")
    val chars = getStringChars(env, this, null)
    return chars!!.toKString()
}

/* main */

fun main() {
    takeScreenshot(destinationUrl = "file:///Users/garrison/Desktop/test.png")
}