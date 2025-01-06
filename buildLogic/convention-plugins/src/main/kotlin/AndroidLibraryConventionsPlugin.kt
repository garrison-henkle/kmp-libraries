import com.android.build.gradle.LibraryExtension
import dev.henkle.conventions.getIntProperty
import dev.henkle.conventions.getJavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType

@Suppress("unused")
class AndroidLibraryConventionsPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")
            extensions.getByType<LibraryExtension>().apply {
                compileSdk = getIntProperty(name = "android.sdk.compile")

                sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

                defaultConfig {
                    minSdk = getIntProperty(name = "android.sdk.min")
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
        }
    }
}