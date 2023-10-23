import org.jetbrains.compose.desktop.application.dsl.TargetFormat

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
            }
        }
    }
}
