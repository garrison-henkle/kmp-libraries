package dev.henkle.utils

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import java.util.Properties

@Suppress("FunctionName")
fun Project._getProperty(name: String): String? = findProperty(name) as? String

fun Project.getStringProperty(name: String): String = _getProperty(name = name)
    ?: throw Exception("Property '$name' does not exist")

fun Project.getIntProperty(name: String): Int = getStringProperty(name = name).toIntOrNull()
    ?: throw Exception("Property '$name' is not an Int")

fun Project.getJavaVersion(name: String = "java.version"): JavaVersion {
    val javaVersion = getStringProperty(name = name).replace('.', '_')
    return try {
        JavaVersion.valueOf("VERSION_$javaVersion")
    } catch (ex: Exception) {
        throw Exception("Property '$name' is an invalid Java version")
    }
}

private var localPropertiesInstance: Properties? = null
val Project.localProperties: Properties get() = localPropertiesInstance ?: run {
    file("${project.rootDir}/local.properties").inputStream().use {
        Properties().apply { load(it) }.also { localPropertiesInstance = it }
    }
}

val Project.configureNative: Boolean get() = localProperties
    .getProperty("native.configuration.disabled")
    ?.toBooleanStrict()?.not() ?: true

//fun Project.configurePublishingToPersonalMavenRepo() {
//    apply(plugin = "maven-publish")
//    the<PublishingExtension>().apply {
//        val mavenName = localProperties.getProperty("maven.name")
//            ?: System.getenv("MAVEN_NAME")
//        val mavenUrl = localProperties.getProperty("maven.url")
//            ?: System.getenv("MAVEN_URL")
//        val mavenUsername = localProperties.getProperty("maven.username")
//            ?: System.getenv("MAVEN_USERNAME")
//        val mavenPassword = localProperties.getProperty("maven.password")
//            ?: System.getenv("MAVEN_PASSWORD")
//
//        repositories {
//            maven {
//                name = mavenName
//                url = uri(mavenUrl)
//                credentials {
//                    username = mavenUsername
//                    password = mavenPassword
//                }
//            }
//        }
//    }
//}
