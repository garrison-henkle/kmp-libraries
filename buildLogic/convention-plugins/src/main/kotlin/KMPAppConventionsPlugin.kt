
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import dev.henkle.conventions.configureNative
import dev.henkle.conventions.getIntProperty
import dev.henkle.conventions.getJavaVersion
import dev.henkle.conventions.getStringProperty
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.get
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetWithHostTests
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinWasmJsTargetDsl
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

class KMPAppConventionsPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.application")
            pluginManager.apply("org.jetbrains.kotlin.multiplatform")
            pluginManager.apply(PublishingPlugin::class)
        }
    }

    companion object {
        private val allTargets = setOf("ios", "macos", "desktop", "js", "wasm")

        fun defaultConfiguration(
            project: Project,
            packageName: String? = null,
            versionCode: String? = null,
            versionName: String? = null,
            iosFrameworkName: String? = null,
            macosEntryPoint: String? = null,
            webModuleName: String? = null,
            webWebpackOutputFilename: String? = null,
            jvmTargetName: String = "desktop",
            androidManifestPlaceholders: Map<String, String> = emptyMap(),
            enabledTargets: Set<String> = allTargets,
            androidConfiguration: (KotlinAndroidTarget.() -> Unit)? = null,
            jvmConfiguration: (KotlinJvmTarget.() -> Unit)? = null,
            darwinConfiguration: (KotlinNativeTarget.() -> Unit)? = null,
            iOSConfiguration: (KotlinNativeTarget.() -> Unit)? = null,
            macOSConfiguration: (KotlinNativeTargetWithHostTests.() -> Unit)? = null,
            wasmJsConfiguration: (KotlinWasmJsTargetDsl.() -> Unit)? = null,
            jsConfiguration: (KotlinJsTargetDsl.() -> Unit)? = null,
        ) {
            with(project) {
                val namespaceProperty = packageName
                    ?: throw IllegalStateException("packageName is null!")
                val versionCodeProperty = versionCode
                    ?: throw IllegalStateException("versionCode is null!")
                val versionNameProperty = versionName
                    ?: throw IllegalStateException("versionName is null!")
                val namespace = getStringProperty(name = namespaceProperty)
                val code = getIntProperty(name = versionCodeProperty)
                val name = getStringProperty(name = versionNameProperty)

                extensions.configure<BaseAppModuleExtension>("android") {
                    this.namespace = namespace
                    compileSdk = getIntProperty(name = "android.sdk.compile")

                    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
                    sourceSets["main"].res.srcDirs("src/androidMain/res")
                    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

                    defaultConfig {
                        minSdk = getIntProperty(name = "android.sdk.min")
                        targetSdk = getIntProperty(name = "android.sdk.target")
                        this.versionCode = code
                        this.versionName = name
                        for ((placeholder, value) in androidManifestPlaceholders) {
                            manifestPlaceholders[placeholder] = value
                        }
                    }

                    packaging {
                        resources {
                            excludes += "/META-INF/{AL2.0,LGPL2.1}"
                        }
                    }

                    buildTypes {
                        getByName("release") {
                            isMinifyEnabled = false
                        }
                    }

                    compileOptions {
                        sourceCompatibility = getJavaVersion()
                        targetCompatibility = getJavaVersion()
                    }
                }

                project.extensions.configure<KotlinMultiplatformExtension>("kotlin") {
                    val androidTarget = androidTarget {
                        compilations.all {
                            kotlinOptions {
                                jvmTarget = getStringProperty(name = "java.version")
                            }
                        }
                        (project.extensions.findByName("android") as BaseAppModuleExtension).applicationVariants.forEach {
                            System.err.println("Found variant: ${it.name}")
                        }
                    }
                    if (androidConfiguration != null) {
                        androidTarget.androidConfiguration()
                    }

                    if ("desktop" in enabledTargets) {
                        val jvmTarget = jvm(name = jvmTargetName)
                        if (jvmConfiguration != null) {
                            jvmTarget.jvmConfiguration()
                        }
                    }

                    val iosEnabled = "ios" in enabledTargets
                    val iosTargets = if (iosEnabled) {
                        arrayOf(
                            iosArm64(),
                            iosSimulatorArm64(),
                        )
                    } else {
                        emptyArray()
                    }

                    val macosEnabled = "macos" in enabledTargets
                    val macosTargets = if (macosEnabled) {
                        arrayOf(
                            macosArm64(),
                            macosX64(),
                        )
                    } else {
                        emptyArray()
                    }

                    if (configureNative) {
                        if (iosEnabled) {
                            iosTargets.forEach {
                                it.binaries.framework {
                                    val frameworkNameProperty = iosFrameworkName
                                        ?: throw IllegalStateException("iosFrameworkName was null!")
                                    baseName = getStringProperty(name = frameworkNameProperty)
                                    isStatic = true
                                }
                                if (iOSConfiguration != null) {
                                    it.iOSConfiguration()
                                }
                            }
                        }

                        if (macosEnabled && macOSConfiguration != null) {
                            macosTargets.forEach {
                                it.binaries.executable {
                                    entryPoint = macosEntryPoint
                                }
                                it.macOSConfiguration()
                            }
                        }

                        if ((iosEnabled || macosEnabled) && darwinConfiguration != null) {
                            (iosTargets + macosTargets).forEach { it.darwinConfiguration() }
                        }
                    }

                    val wasmEnabled = "wasm" in enabledTargets
                    val jsEnabled = "js" in enabledTargets
                    if (wasmEnabled || jsEnabled) {
                        val moduleNameProperty = webModuleName
                            ?: throw IllegalStateException("webModuleName was null!")
                        val moduleName = getStringProperty(name = moduleNameProperty)
                        val webpackOutputFilenameProperty = webWebpackOutputFilename
                            ?: throw IllegalStateException("webWebpackOutputFilename was null!")
                        val webpackOutputFilename = getStringProperty(name = webpackOutputFilenameProperty)

                        if (wasmEnabled) {
                            @OptIn(ExperimentalWasmDsl::class)
                            wasmJs {
                                this.moduleName = moduleName
                                browser {
                                    commonWebpackConfig {
                                        outputFileName = webpackOutputFilename
                                    }
                                }
                                binaries.executable()
                                if (wasmJsConfiguration != null) {
                                    wasmJsConfiguration()
                                }
                            }
                        }

                        if (jsEnabled) {
                            js(IR) {
                                this.moduleName = moduleName
                                browser {
                                    commonWebpackConfig {
                                        outputFileName = webpackOutputFilename
                                    }
                                }
                                binaries.executable()
                                if (jsConfiguration != null) {
                                    jsConfiguration()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}