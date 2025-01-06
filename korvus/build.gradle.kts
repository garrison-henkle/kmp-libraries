import dev.henkle.utils.getStringProperty

plugins {
    alias(libs.plugins.conventionsKMPLibrary)
    alias(libs.plugins.kotlinxSerialization)
}

group = getStringProperty(name = "group.id")
version = getStringProperty(name = "korvus.version.name")

KMPLibraryConventionsPlugin.defaultConfiguration(
    project = project,
    packageNameProperty = "korvus.package.name",
    enabledTargets = setOf("ios", "desktop", "js"),
    jvmTargetName = "jvm",
)

kotlin {
    applyDefaultHierarchyTemplate()
    sourceSets {
        val androidMain by getting
        val appleMain by getting
        val commonMain by getting
        val commonTest by getting
        val jsMain by getting
        val jvmMain by getting

        commonMain.dependencies {
            implementation(libs.kotlinx.serialization)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.logging)
            implementation(libs.ktor.serialization)
            implementation(projects.nanoid)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(projects.testUtils)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.engine.android)
        }

        appleMain.dependencies {
            implementation(libs.ktor.client.engine.darwin)
        }

        jsMain.dependencies {
            implementation(libs.ktor.client.engine.js)
        }

        jvmMain.dependencies {
            implementation(libs.ktor.client.engine.java)
        }
    }
}
