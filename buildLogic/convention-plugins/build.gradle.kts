plugins {
    `kotlin-dsl`
}

group = "dev.henkle.kmp.builds"

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.multiplatform.gradle.plugin)
}

gradlePlugin {
    plugins {
        // keep all versions at 1.0.0 so the libs.versions.toml always works
        register("androidLibraryConventions") {
            id = "dev.henkle.conventions.android.library"
            implementationClass = "AndroidLibraryConventionsPlugin"
            version = "1.0.0"
        }
        register("androidAppConventions") {
            id = "dev.henkle.conventions.android.app"
            implementationClass = "AndroidAppConventionsPlugin"
            version = "1.0.0"
        }
        register("kmpLibraryConventions") {
            id = "dev.henkle.conventions.kmp.library"
            implementationClass = "KMPLibraryConventionsPlugin"
            version = "1.0.0"
        }
        register("kmpAppConventions") {
            id = "dev.henkle.conventions.kmp.app"
            implementationClass = "KMPAppConventionsPlugin"
            version = "1.0.0"
        }
        register("kmpNonAndroidAppConventions") {
            id = "dev.henkle.conventions.kmp.non.android.app"
            implementationClass = "KMPNonAndroidAppConventionsPlugin"
            version = "1.0.0"
        }
        register("publishingConventions") {
            id = "dev.henkle.conventions.publishing"
            implementationClass = "PublishingPlugin"
            version = "1.0.0"
        }
    }
}
