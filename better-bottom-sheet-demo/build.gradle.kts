plugins {
    alias(libs.plugins.conventionsKMPApp)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.jetbrainsCompose)
}

KMPAppConventionsPlugin.defaultConfiguration(
    project = project,
    packageName = "demo.sheet.package.name",
    versionCode = "demo.sheet.version.code",
    versionName = "demo.sheet.version.name",
    iosFrameworkName = "demo.sheet.framework.name",
    enabledTargets = setOf("ios"),
)

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.kermit)
            implementation(projects.betterBottomSheet)
        }

        all {
            languageSettings.optIn("kotlin.experimental.ExperimentalNativeApi")
        }
    }
}
