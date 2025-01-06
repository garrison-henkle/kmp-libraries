import dev.henkle.utils.getStringProperty

plugins {
    alias(libs.plugins.conventionsKMPLibrary)
}

group = getStringProperty(name = "group.id")
version = getStringProperty(name = "test.version.name")

KMPLibraryConventionsPlugin.defaultConfiguration(
    project = project,
    packageNameProperty = "test.package.name",
    enabledTargets = KMPLibraryConventionsPlugin.allTargets,
    jvmTargetName = "jvm",
)

kotlin {
    applyDefaultHierarchyTemplate()
    sourceSets {
        val commonMain by getting
        val androidMain by getting
        val jsMain by getting
        val jvmMain by getting
        val nativeMain by getting
        val wasmJsMain by getting
        val jvmSharedMain by creating {
            dependsOn(commonMain)
            androidMain.dependsOn(this)
            jvmMain.dependsOn(this)
        }
        val webMain by creating {
            dependsOn(commonMain)
            jsMain.dependsOn(this)
            wasmJsMain.dependsOn(this)
        }

        commonMain.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.coroutines.test)
        }

        jvmSharedMain.dependencies {
            implementation(libs.junit)
        }
    }
}
