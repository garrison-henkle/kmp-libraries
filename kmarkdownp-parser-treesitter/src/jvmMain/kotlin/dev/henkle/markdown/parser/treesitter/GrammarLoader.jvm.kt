package dev.henkle.markdown.parser.treesitter

import kotlin.io.path.pathString
import kotlin.io.path.writeBytes

internal actual fun loadGrammarCLibrary() {
    val sharedLibraryName = "markdown-grammar"
    try {
        System.loadLibrary(sharedLibraryName)
    } catch (ex: UnsatisfiedLinkError) {
        @Suppress("UnsafeDynamicallyLoadedCode")
        System.load(copyLibToTmpAndGetPath(sharedLibraryName = sharedLibraryName) ?: throw ex)
    }
}

private object GrammarLoader

@Suppress("SameParameterValue")
@Throws(UnsupportedOperationException::class)
private fun copyLibToTmpAndGetPath(sharedLibraryName: String): String? {
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
    val libUrl = GrammarLoader.javaClass.getResource(libPath) ?: return null
    return kotlin.io.path.createTempFile(
        prefix = "$prefix$sharedLibraryName",
        suffix = ".$ext"
    ).apply {
        writeBytes(libUrl.openStream().use { it.readAllBytes() })
        toFile().deleteOnExit()
    }.pathString
}
