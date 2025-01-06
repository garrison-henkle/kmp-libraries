plugins {
    alias(libs.plugins.conventionsKMPApp)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.jetbrainsCompose)
}

KMPAppConventionsPlugin.defaultConfiguration(
    project = project,
    packageName = "demo.pager.package.name",
    versionCode = "demo.pager.version.code",
    versionName = "demo.pager.version.name",
    enabledTargets = emptySet(),
)

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.kermit)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(projects.pager)
        }
    }
}
