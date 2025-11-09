import dev.henkle.utils.getStringProperty

plugins {
    alias(libs.plugins.conventionsKMPLibrary)
}

group = getStringProperty(name = "group.id")
version = getStringProperty(name = "queue.version.name")

KMPLibraryConventionsPlugin.defaultConfiguration(
    project = project,
    packageNameProperty = "queue.package.name",
    jvmTargetName = "jvm",
)
