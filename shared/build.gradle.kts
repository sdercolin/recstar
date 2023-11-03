import java.util.*

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "1.9.10"
    id("com.android.library")
    id("org.jetbrains.compose")
}

val localProperties =
    Properties().apply {
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { load(it) }
        }
    }
val isMac = System.getProperty("os.name").lowercase().contains("mac")
val includeIos = isMac && localProperties.getProperty("ios.disabled", "false").toBoolean().not()

val voyagerVersion = "1.0.0-rc08"

fun voyager(module: String) = "cafe.adriel.voyager:voyager-$module:$voyagerVersion"

kotlin {
    androidTarget()

    jvm("desktop")

    if (includeIos) {
        listOf(
            iosX64(),
            iosArm64(),
            iosSimulatorArm64(),
        ).forEach { iosTarget ->
            iosTarget.binaries.framework {
                baseName = "shared"
                linkerOpts("-framework", "AVFoundation")
                isStatic = true
            }
        }
    }

    targets.all {
        compilations.all {
            compilerOptions.configure {
                freeCompilerArgs = listOf("-Xexpect-actual-classes")
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.materialIconsExtended)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
                implementation("io.github.aakira:napier:2.6.1")
                implementation(voyager("navigator"))
                implementation(voyager("transitions"))
            }
        }
        val sharedJvmMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation("org.apache.tika:tika-parser-text-module:2.4.1")
            }
        }
        val androidMain by getting {
            dependsOn(sharedJvmMain)
            dependencies {
                api("androidx.activity:activity-compose:1.8.0")
                api("androidx.appcompat:appcompat:1.6.1")
                api("androidx.core:core-ktx:1.12.0")
            }
        }
        if (includeIos) {
            val iosX64Main by getting
            val iosArm64Main by getting
            val iosSimulatorArm64Main by getting
            val iosMain by creating {
                dependsOn(commonMain)
                iosX64Main.dependsOn(this)
                iosArm64Main.dependsOn(this)
                iosSimulatorArm64Main.dependsOn(this)
            }
        }
        val desktopMain by getting {
            dependsOn(sharedJvmMain)
            dependencies {
                implementation(compose.desktop.common)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.7.3")
                val lwjglVersion = "3.3.1"
                listOf("lwjgl", "lwjgl-nfd").forEach { lwjglDep ->
                    implementation("org.lwjgl:$lwjglDep:$lwjglVersion")
                    if (System.getProperty("os.name").startsWith("win", ignoreCase = true)) {
                        listOf("natives-windows", "natives-windows-x86", "natives-windows-arm64").forEach { native ->
                            runtimeOnly("org.lwjgl:$lwjglDep:$lwjglVersion:$native")
                        }
                    }
                }
            }
        }
    }
}

android {
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    namespace = "com.sdercolin.recstar.common"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = (findProperty("android.minSdk") as String).toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
}
