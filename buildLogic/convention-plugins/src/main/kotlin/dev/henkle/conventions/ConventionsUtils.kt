package dev.henkle.conventions

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType
import java.util.Properties

@Suppress("FunctionName")
private fun Project._getProperty(name: String): String? = findProperty(name) as? String

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
val Project.localProperties: Properties
    get() = localPropertiesInstance ?: run {
    file("${project.rootDir}/local.properties").inputStream().use {
        Properties().apply { load(it) }.also { localPropertiesInstance = it }
    }
}

val Project.configureNative: Boolean get() = localProperties
    .getProperty("native.configuration.disabled")
    ?.toBooleanStrict()?.not() ?: true

val Project.libs
    get(): VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
