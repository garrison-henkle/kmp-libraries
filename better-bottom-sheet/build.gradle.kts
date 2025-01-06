
import dev.henkle.utils.getStringProperty

plugins {
    alias(libs.plugins.conventionsKMPLibrary)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.jetbrainsCompose)
}

group = getStringProperty(name = "group.id.compose")
version = getStringProperty(name = "sheet.version.name")

KMPLibraryConventionsPlugin.defaultConfiguration(project, "sheet.package.name")

kotlin {
    applyDefaultHierarchyTemplate()
    sourceSets {
        val commonMain by getting
        val desktopMain by getting
        val appleMain by getting
        val jsMain by getting
        val wasmJsMain by getting

        val skikoMain by creating {
            dependsOn(commonMain)
            desktopMain.dependsOn(this)
            appleMain.dependsOn(this)
            jsMain.dependsOn(this)
            wasmJsMain.dependsOn(this)
        }

        commonMain.dependencies {
            implementation(compose.animation)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.runtime)
            implementation(libs.kermit)
        }
    }
}
