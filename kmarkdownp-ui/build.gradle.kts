import dev.henkle.utils.getStringProperty

plugins {
    alias(libs.plugins.conventionsKMPLibrary)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.jetbrainsCompose)
}

group = getStringProperty(name = "group.id.compose")
version = getStringProperty(name = "markdown.ui.version.name")

KMPLibraryConventionsPlugin.defaultConfiguration(
    project = project,
    packageNameProperty = "markdown.ui.package.name",
    enabledTargets = setOf("ios", "desktop", "wasm")
)

kotlin {
    applyDefaultHierarchyTemplate()
    sourceSets {
        val desktopMain by getting
        commonMain.dependencies {
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.runtime)
            implementation(libs.coil)
            implementation(libs.coil.compose)
            implementation(libs.coil.network)
            implementation(libs.kermit)
            implementation(libs.kotlinx.atomic.fu)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.lazy.table)
            implementation(projects.kmarkdownp)
        }

        desktopMain.dependencies {
            implementation(libs.ktor.client.engine.java)
        }
    }
}
