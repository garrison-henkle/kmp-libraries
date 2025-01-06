import dev.henkle.utils.configureNative

plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    jvm {
        withJava()
        compilations {
            val main = getByName(name = "main")
            tasks {
                register<Jar>("buildFatJarWithNativeLibs") {
                    group = "application"
                    dependsOn("jvmMainClasses", "jvmProcessResources", "compileKotlinJvm")
                    manifest {
                        attributes["Main-Class"] = "dev.henkle.screenshots.MainKt"
                    }
                    from(configurations.getByName("runtimeClasspath").map { if(it.isDirectory) it else zipTree(it) }, main.output.classesDirs)
                    from(main.defaultSourceSet.resources.sourceDirectories.asFileTree)
                    archiveBaseName = "${project.name}-fatWithNativeLibs"
                }
            }
        }
    }

    val macosTargets = listOf(
        macosArm64(),
        macosX64(),
    )

    if (configureNative) {
        macosTargets.forEach { macosTarget ->
            macosTarget.compilations["main"].cinterops.create("jni") {
                val javaHomePath = System.getenv("JAVA_HOME")
                packageName = "dev.henkle.screenshots.jni"
                includeDirs(
                    Callable { File("$javaHomePath/include") },
                    Callable { File("$javaHomePath/include/darwin") },
                )
            }
            macosTarget.binaries {
                executable {
                    entryPoint = "dev.henkle.screenshots.main"
                }
                sharedLib()
            }
        }
    }

    sourceSets {
        jvmMain.dependencies {
            implementation(libs.jvm.native.lib.loader)
        }
    }
}
