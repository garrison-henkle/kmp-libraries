
import dev.henkle.utils.configureNative
import dev.henkle.utils.getStringProperty

plugins {
    alias(libs.plugins.conventionsKMPLibrary)
    id("io.github.ttypic.swiftklib")
}

KMPLibraryConventionsPlugin.defaultConfiguration(
    project = project,
    packageNameProperty = "payments.package.name",
    darwinConfiguration = {
        compilations {
            getByName(name = "main") {
                cinterops.create(getStringProperty(name = "payments.cinterop.name"))
            }
        }
    },
)

kotlin {
    applyDefaultHierarchyTemplate()
    sourceSets {
        val commonMain by getting
        val jsMain by getting
        val wasmJsMain by getting

        val webMain by creating {
            dependsOn(commonMain)
            jsMain.dependsOn(this)
            wasmJsMain.dependsOn(this)
        }

        commonMain.dependencies {
            implementation(libs.bignum)
            implementation(libs.kermit)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.uuid)
        }

        androidMain.dependencies {
            implementation(libs.google.play.billing)
            implementation(projects.contextProvider)
        }
    }
}

if(configureNative) {
    swiftklib {
        create(getStringProperty(name = "payments.cinterop.name")) {
            path = file(path = getStringProperty(name = "payments.cinterop.path"))
            packageName(name = getStringProperty(name = "payments.cinterop.package.name"))
            minIos = 15
            minMacos = 12
        }
    }
}
