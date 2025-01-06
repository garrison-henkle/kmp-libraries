import KMPLibraryConventionsPlugin.Companion.allTargets
import dev.henkle.utils.getStringProperty

plugins {
    alias(libs.plugins.conventionsKMPLibrary)
}

group = getStringProperty(name = "group.id")
version = getStringProperty(name = "markdown.jetbrains.version.name")

KMPLibraryConventionsPlugin.defaultConfiguration(
    project = project,
    packageNameProperty = "markdown.jetbrains.package.name",
    enabledTargets = allTargets,
    jvmTargetName = "jvm",
)

kotlin {
    applyDefaultHierarchyTemplate()
    sourceSets {
        val commonMain by getting

        commonMain.dependencies {
            implementation(libs.jetbrains.markdown)
            compileOnly(projects.kmarkdownp)
            api(projects.kmarkdownp)
        }
    }
}
