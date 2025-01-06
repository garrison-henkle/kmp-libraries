
import dev.henkle.utils.getStringProperty

plugins {
    alias(libs.plugins.conventionsAndroidLibrary)
    alias(libs.plugins.conventionsPublishing)
    alias(libs.plugins.kotlinMultiplatform)
}

group = getStringProperty(name = "group.id")
version = getStringProperty(name = "context.version.name")

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = getStringProperty(name = "java.version")
            }
        }
        publishLibraryVariants("release")
    }
}

android {
    namespace = getStringProperty(name = "context.package.name")
}
