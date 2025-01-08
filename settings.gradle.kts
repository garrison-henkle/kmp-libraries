import java.util.Properties

rootProject.name = "KMP"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

val localProperties = Properties()
rootDir.resolve("local.properties").inputStream().use {
    localProperties.load(it)
}

pluginManagement {
    includeBuild("buildLogic")
    repositories {
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
        gradlePluginPortal()
        mavenCentral()
    }
    includeBuild("swift-klib-plugin")
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
        maven("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
        maven {
            setUrl(localProperties.getProperty("maven.url"))
            credentials {
                username = localProperties.getProperty("maven.username")
                password = localProperties.getProperty("maven.password")
            }
        }
    }
}

include(":stytch-kmp-demo")
include(":stytch-kmp")
include(":keymp")
include(":keymp-preferences")
include(":context-provider")
include(":better-bottom-sheet")
include(":better-bottom-sheet-demo")
include(":pager")
include(":pager-demo")
//include(":kmpayments")
//include(":macos-screenshots")
include(":kmpress")
include(":kmarkdownp")
include(":kmarkdownp-parser-jetbrains")
//include(":kmarkdownp-parser-treesitter")
include(":kmarkdownp-ui")
include(":kmarkdownp-ui-demo")
include(":korvus")
include(":nanoid")
include(":test-utils")
include(":utils")
include(":surreal-kmp")