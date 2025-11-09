import dev.henkle.utils.getStringProperty

plugins {
    alias(libs.plugins.conventionsKMPLibrary)
}

group = getStringProperty(name = "group.id")
version = getStringProperty(name = "encoding.url.version.name")

KMPLibraryConventionsPlugin.defaultConfiguration(
    project = project,
    packageNameProperty = "encoding.url.package.name",
    jvmTargetName = "jvm",
)

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(project(":data-structures:bitset"))
    }
}
