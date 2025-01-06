
import com.codingfeline.buildkonfig.compiler.FieldSpec
import dev.henkle.utils.getStringProperty
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.buildkonfig)
    alias(libs.plugins.conventionsKMPLibrary)
    alias(libs.plugins.kotlinxSerialization)
}

group = getStringProperty(name = "group.id")
version = getStringProperty(name = "stytch.version.name")

KMPLibraryConventionsPlugin.defaultConfiguration(project, "stytch.package.name")

kotlin {
    applyDefaultHierarchyTemplate()
    sourceSets {
        val androidMain by getting
        val appleMain by getting
        val commonMain by getting
        val desktopMain by getting
        val iosMain by getting
        val jsMain by getting
        val macosMain by getting
        val wasmJsMain by getting

        val jvmMain by creating {
            dependsOn(commonMain)
            androidMain.dependsOn(this)
            desktopMain.dependsOn(this)
        }
        val mobileMain by creating {
            dependsOn(commonMain)
            androidMain.dependsOn(this)
            iosMain.dependsOn(this)
        }
        val nonWebMain by creating {
            dependsOn(commonMain)
            appleMain.dependsOn(this)
            jvmMain.dependsOn(this)
        }
        val pcMain by creating {
            dependsOn(commonMain)
            desktopMain.dependsOn(this)
            macosMain.dependsOn(this)
        }
        val webMain by creating {
            dependsOn(commonMain)
            jsMain.dependsOn(this)
            wasmJsMain.dependsOn(this)
        }
        val nonAppleMain by creating {
            dependsOn(commonMain)
            jvmMain.dependsOn(this)
            webMain.dependsOn(this)
        }

        commonMain.dependencies {
            implementation(libs.hash)
            implementation(libs.kermit)
            implementation(libs.kotlinx.atomic.fu)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.logging)
            implementation(libs.ktor.serialization)
            implementation(libs.secure.random)
            implementation(libs.uuid)
            implementation(projects.keymp)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity)
            implementation(libs.androidx.browser)
            implementation(libs.ktor.client.engine.android)
            implementation(projects.contextProvider)
        }

        appleMain.dependencies {
            implementation(libs.ktor.client.engine.darwin)
        }

        desktopMain.dependencies {
            implementation(libs.ktor.client.engine.java)
        }

        pcMain.dependencies {
            implementation(libs.ktor.server.core)
            implementation(libs.ktor.server.engine.cio)
        }

        wasmJsMain.dependencies {
            // https://github.com/Kotlin/kotlinx-browser
            // this is hosted on my personal maven for now - it's not on mavenCentral
            // hopefully there will be a version that supports js and wasmJs soon as well...
            implementation(libs.kotlinx.browser)
        }

        webMain.dependencies {
            implementation(libs.ktor.client.engine.js)
        }

        all {
            languageSettings.optIn("kotlin.experimental.ExperimentalNativeApi")
        }
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xexpect-actual-classes",
        )
    }
}

buildkonfig {
    packageName = getStringProperty(name = "stytch.package.name")

    defaultConfigs {
        buildConfigField(
            type = FieldSpec.Type.STRING,
            name = "versionName",
            value = getStringProperty(name = "stytch.version.name"),
        )

        buildConfigField(
            type = FieldSpec.Type.INT,
            name = "versionCode",
            value = getStringProperty(name = "stytch.version.code")
        )
    }
}
