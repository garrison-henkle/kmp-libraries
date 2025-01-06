
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import dev.henkle.utils.getStringProperty
import dev.henkle.utils.localProperties
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

plugins {
    alias(libs.plugins.conventionsKMPApp)
    alias(libs.plugins.buildkonfig)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.jetbrainsCompose)
}

KMPAppConventionsPlugin.defaultConfiguration(
    project = project,
    packageName = "demo.stytch.package.name",
    versionCode = "demo.stytch.version.code",
    versionName = "demo.stytch.version.name",
    iosFrameworkName = "demo.stytch.framework.name",
    macosEntryPoint = "demo.stytch.macos.entry.point",
    webModuleName = "demo.stytch.web.module.name",
    webWebpackOutputFilename = "demo.stytch.web.output.filename",
    enabledTargets = setOf("ios", "desktop", "web"),
    androidManifestPlaceholders = mapOf(
        "stytchOAuthRedirectScheme" to "stytchkmp",
        "stytchOAuthRedirectHost" to "callback",
    ),
)

kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyHierarchyTemplate {
        withSourceSetTree(KotlinSourceSetTree.main)

        common {
            withCompilations { true }

            group("jvm") {
                withAndroidTarget()
                withJvm()
            }

            group("ios") {
                withIos()
            }

            group("macos") {
                withMacos()
            }
        }
    }

    sourceSets {
        val desktopMain by getting

        androidMain.dependencies {
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.kermit)
            implementation(projects.stytchKmp)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.logback)
        }

        all {
            languageSettings.optIn("kotlin.experimental.ExperimentalNativeApi")
        }
    }
}

compose.desktop {
    application {
        mainClass = "dev.henkle.stytch.demo.Main_desktopKt"

        fromFiles(rootProject.fileTree("stytch-kmp-demo/src/desktopMain/resources"))

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi)
            packageName = getStringProperty(name = "demo.stytch.package.name")
            packageVersion = getStringProperty(name = "demo.stytch.version.name")
//            macOS {
//                signing {
//                    this.identity
//                    this.sign
//                    this.prefix
//                    this.keychain
//                }
//                notarization {
//                    this.teamID
//                    this.appleID
//                    this.password
//                }
//                this.minimumSystemVersion
//                this.packageName
//                this.packageVersion
//                this.packageBuildVersion
//                this.entitlementsFile
//                this.runtimeEntitlementsFile
//                this.provisioningProfile
//                this.runtimeProvisioningProfile
//            }
        }
    }
}

compose.experimental {
    web.application {}
}


buildkonfig {
    packageName = getStringProperty(name = "demo.stytch.package.name")

    defaultConfigs {
        buildConfigField(
            type = STRING,
            name = "stytchPublicToken",
            value = localProperties.getProperty("stytch.token.test")
                ?: throw Exception("'stytch.token.test' is missing from local.properties")
        )
    }
}
