import dev.henkle.conventions.configureNative
import dev.henkle.conventions.getStringProperty
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetWithHostTests
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinWasmJsTargetDsl
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

class KMPNonAndroidAppConventionsPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.multiplatform")
            pluginManager.apply(PublishingPlugin::class)
        }
    }

    companion object {
        private val allTargets = setOf("ios", "macos", "desktop", "js", "wasm")

        fun defaultConfiguration(
            project: Project,
            iosFrameworkName: String? = null,
            macosEntryPoint: String? = null,
            webModuleName: String? = null,
            webWebpackOutputFilename: String? = null,
            jvmTargetName: String = "desktop",
            enabledTargets: Set<String> = allTargets,
            jvmConfiguration: (KotlinJvmTarget.() -> Unit)? = null,
            darwinConfiguration: (KotlinNativeTarget.() -> Unit)? = null,
            iOSConfiguration: (KotlinNativeTarget.() -> Unit)? = null,
            macOSConfiguration: (KotlinNativeTargetWithHostTests.() -> Unit)? = null,
            wasmJsConfiguration: (KotlinWasmJsTargetDsl.() -> Unit)? = null,
            jsConfiguration: (KotlinJsTargetDsl.() -> Unit)? = null,
        ) {
            with(project) {
                project.extensions.configure<KotlinMultiplatformExtension>("kotlin") {
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