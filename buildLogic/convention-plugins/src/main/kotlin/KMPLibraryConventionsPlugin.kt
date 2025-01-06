
import com.android.build.gradle.LibraryExtension
import dev.henkle.conventions.configureNative
import dev.henkle.conventions.getStringProperty
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetWithHostTests
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinWasmJsTargetDsl
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

class KMPLibraryConventionsPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply(AndroidLibraryConventionsPlugin::class)
        pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        pluginManager.apply(PublishingPlugin::class)
    }

    companion object {
        val allTargets = setOf("ios", "macos", "desktop", "js", "wasm", "linux")
        val defaultTargets = setOf("ios", "macos", "desktop", "js", "wasm")

        @OptIn(ExperimentalWasmDsl::class)
        fun defaultConfiguration(
            project: Project,
            packageNameProperty: String,
            jvmTargetName: String = "desktop",
            enabledTargets: Set<String> = defaultTargets,
            androidConfiguration: (KotlinAndroidTarget.() -> Unit)? = null,
            jvmConfiguration: (KotlinJvmTarget.() -> Unit)? = null,
            darwinConfiguration: (KotlinNativeTarget.() -> Unit)? = null,
            iOSConfiguration: (KotlinNativeTarget.() -> Unit)? = null,
            macOSConfiguration: (KotlinNativeTargetWithHostTests.() -> Unit)? = null,
            linuxX86Configuration: (KotlinNativeTargetWithHostTests.() -> Unit)? = null,
            linuxArm64Configuration: (KotlinNativeTarget.() -> Unit)? = null,
            linuxConfiguration: (KotlinNativeTarget.() -> Unit)? = null,
            wasmJsConfiguration: (KotlinWasmJsTargetDsl.() -> Unit)? = null,
            jsConfiguration: (KotlinJsTargetDsl.() -> Unit)? = null,
        ) {
            with(project.extensions.getByType<KotlinMultiplatformExtension>()) {
                val androidTarget = androidTarget {
                    compilerOptions {
                        jvmTarget = JvmTarget.fromTarget(project.getStringProperty(name = "java.version"))
                    }
                    publishLibraryVariants("release")
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

                if(project.configureNative) {
                    if (iosEnabled && iOSConfiguration != null) {
                        iosTargets.forEach { it.iOSConfiguration() }
                    }

                    if (macosEnabled && macOSConfiguration != null) {
                        macosTargets.forEach { it.macOSConfiguration() }
                    }

                    if ((iosEnabled || macosEnabled) && darwinConfiguration != null) {
                        (iosTargets + macosTargets).forEach { it.darwinConfiguration() }
                    }
                }

                if ("wasm" in enabledTargets) {
                    wasmJs {
                        browser()
                        if (wasmJsConfiguration != null) {
                            wasmJsConfiguration()
                        }
                    }
                }

                if ("js" in enabledTargets) {
                    js(IR) {
                        browser()
                        if (jsConfiguration != null) {
                            jsConfiguration()
                        }
                    }
                }

                if ("linux" in enabledTargets) {
                    arrayOf(
                        linuxArm64 {
                            if (linuxArm64Configuration != null) {
                                linuxArm64Configuration()
                            }
                        },
                        linuxX64 {
                            if (linuxX86Configuration != null) {
                                linuxX86Configuration()
                            }
                        },
                    ).forEach {
                        if (linuxConfiguration != null) {
                            it.linuxConfiguration()
                        }
                    }
                }

                compilerOptions {
                    freeCompilerArgs.add("-Xexpect-actual-classes")
                }
            }

            with(project.extensions.getByType<LibraryExtension>()) {
                namespace = project.getStringProperty(name = packageNameProperty)
            }
        }
    }
}