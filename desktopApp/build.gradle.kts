import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.internal.utils.localPropertiesFile
import java.util.*

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

kotlin {
    jvm()
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(project(":shared"))
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "RecStar"
            packageVersion = "1.0.0" // we don't use this version for actual app versioning
            copyright = "© 2023 sdercolin"
            appResourcesRootDir.set(project.layout.projectDirectory.dir("resources"))
            modules(
                "java.sql",
                "jdk.charsets",
                "jdk.unsupported",
                "jdk.accessibility",
                "java.naming",
            )
            macOS {
                bundleID = "com.sdercolin.recstar"
                iconFile.set(project.file("src/jvmMain/resources/icon.icns"))

                if (project.localPropertiesFile.exists()) {
                    val properties = Properties().apply { load(project.localPropertiesFile.inputStream()) }
                    signing {
                        properties.getOrDefault("compose.desktop.mac.sign", "false").toString().toBoolean()
                            .let { sign.set(it) }
                        properties.getOrDefault("compose.desktop.mac.signing.identity", "").toString()
                            .let { identity.set(it) }
                    }
                    notarization {
                        properties.getOrDefault("compose.desktop.mac.notarization.appleID", "").toString()
                            .let { appleID.set(it) }
                        properties.getOrDefault("compose.desktop.mac.notarization.password", "").toString()
                            .let { password.set(it) }
                    }
                }
            }
            windows {
                iconFile.set(project.file("src/jvmMain/resources/icon.ico"))
            }
            linux {
                iconFile.set(project.file("src/jvmMain/resources/icon.png"))
            }
        }
    }
}
