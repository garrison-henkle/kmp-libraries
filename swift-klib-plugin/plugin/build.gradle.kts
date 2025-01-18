import java.util.Properties

plugins {
    id("java-gradle-plugin")
    alias(libs.plugins.gradlePluginPublish)
    alias(libs.plugins.kotlinJvm)
}

dependencies {
    implementation(gradleApi())
    implementation(libs.kotlin.gradle.plugin)
}

version = "0.5.2"
group = "io.github.ttypic"

val rootProjectGradleProperties = Properties()
file("${rootProject.projectDir}/../gradle.properties").inputStream().use {
    rootProjectGradleProperties.load(it)
}

kotlin {
    val javaVersion = rootProjectGradleProperties.getProperty("java.version").toInt()
    jvmToolchain(javaVersion)
}

gradlePlugin {
    website.set("https://github.com/ttypic/swift-klib-plugin")
    vcsUrl.set("https://github.com/ttypic/swift-klib-plugin")

    plugins {
        create("swiftklib") {
            id = "io.github.ttypic.swiftklib"
            displayName = "SwiftKlib Gradle Plugin"
            description = "Gradle Plugin to inject Swift-code for Kotlin Multiplatform iOS target"
            implementationClass = "io.github.ttypic.swiftklib.gradle.SwiftKlibPlugin"
            tags.set(listOf("kotlin-multiplatform", "swift"))
        }
    }
}

repositories {
    mavenCentral()
}
