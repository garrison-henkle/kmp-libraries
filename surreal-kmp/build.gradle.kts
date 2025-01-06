import com.codingfeline.buildkonfig.compiler.FieldSpec
import dev.henkle.utils.getStringProperty
import org.jetbrains.compose.internal.utils.getLocalProperty

plugins {
    alias(libs.plugins.conventionsKMPLibrary)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.buildkonfig)
}

group = getStringProperty(name = "group.id")
version = getStringProperty(name = "surreal.version.name")

KMPLibraryConventionsPlugin.defaultConfiguration(
    project = project,
    packageNameProperty = "surreal.package.name",
    jvmTargetName = "jvm",
)

kotlin {
    applyDefaultHierarchyTemplate()
    sourceSets {
        val androidMain by getting
        val androidUnitTest by getting
        val appleMain by getting
        val commonMain by getting
        val commonTest by getting
        val jsMain by getting
        val jsTest by getting
        val jvmMain by getting
        val jvmTest by getting
        val wasmJsMain by getting
        val wasmJsTest by getting
        val jvmSharedTest by creating {
            dependsOn(commonTest)
            androidUnitTest.dependsOn(this)
            jvmTest.dependsOn(this)
        }
        val webMain by creating {
            dependsOn(commonMain)
            jsMain.dependsOn(this)
            wasmJsMain.dependsOn(this)
        }
        val webTest by creating {
            dependsOn(commonTest)
            jsTest.dependsOn(this)
            wasmJsTest.dependsOn(this)
        }

        commonMain.dependencies {
            implementation(libs.kermit)
            implementation(libs.kotlinx.atomic.fu)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.sockets)
            implementation(libs.ktor.logging)
            implementation(libs.ktor.serialization)
            implementation(projects.nanoid)
            implementation(projects.utils)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(projects.testUtils)
            implementation(projects.utils)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.engine.okhttp)
        }

        appleMain.dependencies {
            implementation(libs.ktor.client.engine.darwin)
        }

        jvmMain.dependencies {
            implementation(libs.ktor.client.engine.java)
        }

        jvmSharedTest.dependencies {
            implementation(libs.junit)
        }

        webMain.dependencies {
            implementation(libs.ktor.client.engine.js)
        }
    }
}

buildkonfig {
    defaultConfigs {
        packageName = "dev.henkle.surreal"

        buildConfigField(
            type = FieldSpec.Type.STRING,
            name = "surrealUsername",
            value = getLocalProperty(key = "db.user"),
        )

        buildConfigField(
            type = FieldSpec.Type.STRING,
            name = "surrealPassword",
            value = getLocalProperty(key = "db.pass"),
        )
    }
}

//tasks.withType<Test>().configureEach {
//    useJUnitPlatform()
//}
