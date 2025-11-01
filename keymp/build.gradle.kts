
import dev.henkle.conventions.optIntoExpectActualClasses
import dev.henkle.conventions.optIntoNewKotlinFeatures
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
        val commonMain by getting
        val jvmMain by getting
        val webMain by getting

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

        webMain.dependencies {
            implementation(libs.kotlinx.browser)
            implementation(libs.kotlinx.datetime)
        }
    }
}
