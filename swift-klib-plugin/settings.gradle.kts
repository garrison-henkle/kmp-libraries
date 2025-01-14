dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("${rootProject.projectDir}/../gradle/libs.versions.toml"))
        }
    }
}

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "SwiftKlib"
include(":plugin")
