plugins {
    alias(libs.plugins.conventionsKMPApp)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.tree.sitter)
}

KMPAppConventionsPlugin.defaultConfiguration(
    project = project,
    packageName = "demo.markdown.ui.package.name",
    versionCode = "demo.markdown.ui.version.code",
    versionName = "demo.markdown.ui.version.name",
    iosFrameworkName = "demo.markdown.ui.framework.name",
    enabledTargets = setOf("desktop", "android", "ios"),
    jvmTargetName = "jvm",
)

kotlin {
    applyDefaultHierarchyTemplate()
    sourceSets {
        val androidMain by getting
        val commonMain by getting
        val jvmMain by getting
        val iosMain by getting

        val jniMain by creating {
            dependsOn(commonMain)
            androidMain.dependsOn(this)
            jvmMain.dependsOn(this)

//            resources.srcDir("src/jniMain/resources")
        }

        androidMain.dependencies {
            implementation(libs.android.math)
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.engine.android)
        }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.kermit)
            implementation(projects.kmarkdownpUi)
            implementation(projects.kmarkdownpParserJetbrains)
            implementation(projects.kmarkdownpParserTreesitter)

        }

        jvmMain {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.logback)
            }
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.engine.darwin)
        }
    }
}

grammar {
    baseDir = projectDir.resolve("src/native/markdown")
    grammarName = project.name
    className = "TreeSitterMarkdown"
    packageName = "dev.henkle.markdown.grammars"
    files = arrayOf(
        projectDir.resolve("parser.c")
    )
}
