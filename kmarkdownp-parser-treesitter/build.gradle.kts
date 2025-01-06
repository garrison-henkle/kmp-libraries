
import co.touchlab.cklib.gradle.CompileToBitcode.Language.C
import dev.henkle.utils.getStringProperty

plugins {
    alias(libs.plugins.conventionsKMPLibrary)
    alias(libs.plugins.cklib)
}

group = getStringProperty(name = "group.id")
version = getStringProperty(name = "markdown.treesitter.version.name")

KMPLibraryConventionsPlugin.defaultConfiguration(
    project = project,
    packageNameProperty = "markdown.treesitter.package.name",
    enabledTargets = setOf("desktop", "android", "ios"),
    jvmTargetName = "jvm",
    iOSConfiguration = {
        val main by compilations.getting
        main.cinterops {
            create("grammars") {
                packageName = "${getStringProperty(name = "markdown.treesitter.package.name")}.native"
                header(file("src/native/markdown_grammars.h"))
            }
        }
    }
)

kotlin {
    applyDefaultHierarchyTemplate()
    sourceSets {
        val androidMain by getting
        val commonMain by getting
        val jvmMain by getting

        val jniMain by creating {
            dependsOn(commonMain)
            androidMain.dependsOn(this)
            jvmMain.dependsOn(this)
        }

        commonMain.dependencies {
            compileOnly(projects.kmarkdownp)
            api(projects.kmarkdownp)
            implementation(libs.tree.sitter)
        }
    }
}

cklib {
    config.kotlinVersion = libs.versions.kotlin.get()

    create("grammars") {
        language = C
        srcDirs = project.files(
            file("src/native/markdown"),
            file("src/native/markdown-inline"),
        )
    }
}

android {
    externalNativeBuild {
        cmake {
            path("src/androidMain/CMakeLists.txt")
        }
    }
}
