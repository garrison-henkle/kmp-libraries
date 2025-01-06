import dev.henkle.conventions.localProperties
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.kotlin.dsl.the

class PublishingPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.vanniktech.maven.publish")
            the<PublishingExtension>().apply {
                val mavenName = localProperties.getProperty("maven.name")
                    ?: System.getenv("MAVEN_NAME")
                    ?: throw IllegalStateException("'maven.name' is not defined in local.properties!")
                val mavenUrl = localProperties.getProperty("maven.url")
                    ?: System.getenv("MAVEN_URL")
                    ?: throw IllegalStateException("'maven.url' is not defined in local.properties!")
                val mavenUsername = localProperties.getProperty("maven.username")
                    ?: System.getenv("MAVEN_USERNAME")
                    ?: throw IllegalStateException("'maven.username' is not defined in local.properties!")
                val mavenPassword = localProperties.getProperty("maven.password")
                    ?: System.getenv("MAVEN_PASSWORD")
                    ?: throw IllegalStateException("'maven.password' is not defined in local.properties!")

                val maven2Name = localProperties.getProperty("maven2.name")
                    ?: System.getenv("MAVEN2_NAME")
                    ?: throw IllegalStateException("'maven2.name' is not defined in local.properties!")
                val maven2Url = localProperties.getProperty("maven2.url")
                    ?: System.getenv("MAVEN2_URL")
                    ?: throw IllegalStateException("'maven2.url' is not defined in local.properties!")
                val maven2Username = localProperties.getProperty("maven2.username")
                    ?: System.getenv("MAVEN2_USERNAME")
                    ?: throw IllegalStateException("'maven2.username' is not defined in local.properties!")
                val maven2Password = localProperties.getProperty("maven2.password")
                    ?: System.getenv("MAVEN2_PASSWORD")
                    ?: throw IllegalStateException("'maven2.password' is not defined in local.properties!")

                repositories {
                    maven {
                        name = mavenName
                        url = uri(mavenUrl)
                        credentials {
                            username = mavenUsername
                            password = mavenPassword
                        }
                    }

                    maven {
                        name = maven2Name
                        url = uri(maven2Url)
                        credentials {
                            username = maven2Username
                            password = maven2Password
                        }
                    }
                }
            }
        }
    }
}
