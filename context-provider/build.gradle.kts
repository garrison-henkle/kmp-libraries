
import dev.henkle.utils.getStringProperty
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.conventionsAndroidLibrary)
    alias(libs.plugins.conventionsPublishing)
    alias(libs.plugins.kotlinMultiplatform)
}

group = getStringProperty(name = "group.id")
version = getStringProperty(name = "context.version.name")

kotlin {
    // todo(garrison)
//    compilerOptions {
//        jvmTarget.set(JvmTarget.JVM_21)
//    }

    androidTarget {
        publishLibraryVariants("release")
    }
}

android {
    namespace = getStringProperty(name = "context.package.name")
}
