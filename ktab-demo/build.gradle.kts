plugins {
    alias(libs.plugins.conventionsKMPApp)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.kotlinxSerialization)
}

KMPAppConventionsPlugin.defaultConfiguration(
    project = project,
    packageName = "demo.ktab.package.name",
    versionCode = "demo.ktab.version.code",
    versionName = "demo.ktab.version.name",
    enabledTargets = setOf("desktop"),
)

kotlin {
    applyDefaultHierarchyTemplate()
    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.runtime)
            implementation(compose.materialIconsExtended)
            implementation(libs.kermit)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization)
            implementation(project(":ktab"))
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}
