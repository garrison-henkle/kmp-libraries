import dev.henkle.utils.getStringProperty

plugins {
    alias(libs.plugins.conventionsKMPLibrary)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.jetbrainsCompose)
}

group = getStringProperty(name = "group.id.compose")
version = getStringProperty(name = "press.version.name")

KMPLibraryConventionsPlugin.defaultConfiguration(
    project = project,
    packageNameProperty = "press.package.name",
    enabledTargets = setOf("desktop", "wasm", "ios"),
)

kotlin {
    applyDefaultHierarchyTemplate()
    sourceSets {
        commonMain.dependencies {
            implementation(compose.animation)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.runtime)
            implementation(libs.haze)
        }
    }
}
