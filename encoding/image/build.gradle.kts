import dev.henkle.utils.getStringProperty

plugins {
    alias(libs.plugins.conventionsKMPLibrary)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.jetbrainsCompose)
}

group = getStringProperty(name = "group.id")
version = getStringProperty(name = "encoding.image.version.name")

KMPLibraryConventionsPlugin.defaultConfiguration(
    project = project,
    packageNameProperty = "encoding.image.package.name",
    jvmTargetName = "jvm",
)

kotlin {
    applyDefaultHierarchyTemplate()
    sourceSets {
        val androidMain by getting
        val appleMain by getting
        val commonMain by getting
        val jsMain by getting
        val jvmMain by getting
        val wasmJsMain by getting
        val skikoMain by creating {
            this.dependsOn(commonMain)
            appleMain.dependsOn(this)
            jsMain.dependsOn(this)
            jvmMain.dependsOn(this)
            wasmJsMain.dependsOn(this)
        }
        val nonWebMain by creating {
            this.dependsOn(commonMain)
            androidMain.dependsOn(this)
            appleMain.dependsOn(this)
            jvmMain.dependsOn(this)
        }

        commonMain.dependencies {
            implementation(compose.ui)
            implementation(libs.buffer)
            implementation(libs.kermit)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
