
import dev.henkle.utils.getStringProperty
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.conventionsKMPLibrary)
}

group = getStringProperty(name = "group.id")
version = getStringProperty(name = "key.version.name")

KMPLibraryConventionsPlugin.defaultConfiguration(
    project = project,
    packageNameProperty = "key.package.name",
    jvmTargetName = "jvm",
)

kotlin {
    applyDefaultHierarchyTemplate()
    sourceSets {
        val androidMain by getting
        val appleMain by getting
        val commonMain by getting
        val jsMain by getting
        val jvmMain by getting
        val wasmJsMain by getting

        val webMain by creating {
            dependsOn(commonMain)
            jsMain.dependsOn(this)
            wasmJsMain.dependsOn(this)
        }

        commonMain.dependencies {
            implementation(libs.kermit)
        }

        androidMain.dependencies {
            implementation(libs.androidx.security.crypto)
            implementation(projects.contextProvider)
        }

        jvmMain.dependencies {
            implementation(libs.credential.secure.storage)
        }

        wasmJsMain.dependencies {
            // https://github.com/Kotlin/kotlinx-browser
            // this is hosted on my personal maven for now - it's not on mavenCentral
            // hopefully there will be a version that supports js and wasmJs soon as well...
            implementation(libs.kotlinx.browser)
        }

        webMain.dependencies {
            implementation(libs.kotlinx.datetime)
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
