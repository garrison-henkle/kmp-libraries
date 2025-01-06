plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.buildkonfig) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinxSerialization) apply false
    alias(libs.plugins.mavenPublish) apply false
    alias(libs.plugins.tree.sitter) apply false
}

buildscript {
    dependencies {
        classpath(libs.kotlinx.atomic.fu.plugin)
    }
}

apply(plugin = libs.plugins.kotlinxAtomicFu.get().pluginId)
