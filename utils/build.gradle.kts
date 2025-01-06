import dev.henkle.utils.getStringProperty

plugins {
    alias(libs.plugins.conventionsKMPLibrary)
}

group = getStringProperty(name = "group.id")
version = getStringProperty(name = "test.version.name")

KMPLibraryConventionsPlugin.defaultConfiguration(
    project = project,
    packageNameProperty = "test.package.name",
    enabledTargets = KMPLibraryConventionsPlugin.allTargets,
    jvmTargetName = "jvm",
)

kotlin {
    applyDefaultHierarchyTemplate()
    sourceSets {
        val nativeMain by getting
    }
}
