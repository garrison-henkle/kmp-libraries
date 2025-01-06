import KMPLibraryConventionsPlugin.Companion.allTargets
import dev.henkle.utils.getStringProperty

plugins {
    alias(libs.plugins.conventionsKMPLibrary)
}

group = getStringProperty(name = "group.id")
version = getStringProperty(name = "markdown.version.name")

KMPLibraryConventionsPlugin.defaultConfiguration(
    project = project,
    packageNameProperty = "markdown.package.name",
    enabledTargets = allTargets,
    jvmTargetName = "jvm",
)

kotlin {
    applyDefaultHierarchyTemplate()
}
