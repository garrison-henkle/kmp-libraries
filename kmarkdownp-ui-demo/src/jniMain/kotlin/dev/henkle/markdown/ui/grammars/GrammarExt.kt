package dev.henkle.markdown.ui.grammars

import kotlin.io.path.createTempFile
import kotlin.io.path.pathString
import kotlin.io.path.writeBytes

@Throws(UnsupportedOperationException::class)
fun Grammar.copyLibToTmpAndGetPath(): String? {
    val osName = System.getProperty("os.name")?.lowercase()
        ?: throw Exception("Unable to get OS name!")
    val archName = System.getProperty("os.arch")?.lowercase()
        ?: throw Exception("Unable to get device architecture!")
    val ext: String
    val os: String
    val prefix: String
    when {
        "windows" in osName -> {
            ext = "dll"
            os = "windows"
            prefix = ""
        }
        "linux" in osName -> {
            ext = "so"
            os = "linux"
            prefix = "lib"
        }
        "mac" in osName -> {
            ext = "dylib"
            os = "macos"
            prefix = "lib"
        }
        else -> {
            throw UnsupportedOperationException("Unsupported operating system: $osName")
        }
    }
    val arch = when {
        "amd64" in archName || "x86_64" in archName -> "x64"
        "aarch64" in archName || "arm64" in archName -> "aarch64"
        else -> throw UnsupportedOperationException("Unsupported architecture: $archName")
    }
    val libPath = "/lib/$os/$arch/$prefix$sharedLibraryName.$ext"
    val libUrl = javaClass.getResource(libPath) ?: return null
    return createTempFile(
        prefix = "$prefix$sharedLibraryName",
        suffix = ".$ext"
    ).apply {
        writeBytes(libUrl.openStream().use { it.readAllBytes() })
        toFile().deleteOnExit()
    }.pathString.also {
        System.err.println("extracted lib to temp file at $it")
    }
}
