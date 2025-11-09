import dev.henkle.utils.getStringProperty

plugins {
    alias(libs.plugins.conventionsKMPLibrary)
}

group = getStringProperty(name = "group.id")
version = getStringProperty(name = "radix.version.name")

KMPLibraryConventionsPlugin.defaultConfiguration(
    project = project,
    packageNameProperty = "radix.package.name",
    jvmTargetName = "jvm",
)
