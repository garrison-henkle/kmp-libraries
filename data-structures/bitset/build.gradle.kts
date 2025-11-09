import dev.henkle.utils.getStringProperty

plugins {
    alias(libs.plugins.conventionsKMPLibrary)
}

group = getStringProperty(name = "group.id")
version = getStringProperty(name = "bitset.version.name")

KMPLibraryConventionsPlugin.defaultConfiguration(
    project = project,
    packageNameProperty = "bitset.package.name",
    jvmTargetName = "jvm",
)
