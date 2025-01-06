import dev.henkle.utils.getStringProperty

plugins {
    alias(libs.plugins.conventionsKMPLibrary)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.jetbrainsCompose)
}

group = getStringProperty(name = "group.id.compose")
version = getStringProperty(name = "pager.version.name")

KMPLibraryConventionsPlugin.defaultConfiguration(project, "pager.package.name")

kotlin {
    applyDefaultHierarchyTemplate()
    sourceSets {
        commonMain.dependencies {
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.runtime)
            implementation(libs.kermit)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.atomic.fu)
            implementation(libs.uuid)
        }
    }
}
