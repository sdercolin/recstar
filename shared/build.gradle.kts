import com.codingfeline.buildkonfig.compiler.FieldSpec
import com.github.jk1.license.render.JsonReportRenderer
import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import java.util.*

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.20")
        classpath("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
        classpath("com.codingfeline.buildkonfig:buildkonfig-gradle-plugin:0.15.1")
    }
}

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "1.9.10"
    id("com.android.library")
    id("org.jetbrains.compose")
    id("com.codingfeline.buildkonfig") version "+"
    id("com.github.jk1.dependency-license-report") version "2.5"
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

val voyagerVersion = "1.0.0"

fun voyager(module: String) = "cafe.adriel.voyager:voyager-$module:$voyagerVersion"

kotlin {
    androidTarget()

    jvm("desktop")

    if (includeIos) {
        listOf(
            iosX64("iosX64"),
            iosArm64("iosArm64"),
            iosSimulatorArm64("iosSimulatorArm64"),
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
                implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.3.0")
                implementation("io.github.aakira:napier:2.6.1")
                api(voyager("navigator"))
                api(voyager("transitions"))
                api(voyager("screenmodel"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
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
                api("androidx.activity:activity-compose:1.8.2")
                api("androidx.appcompat:appcompat:1.6.1")
                api("androidx.core:core-ktx:1.12.0")
                implementation("androidx.documentfile:documentfile:1.0.1")
            }
        }
        val desktopTest by getting {
            dependencies {
                implementation("com.github.psambit9791:jdsp:1.0.0")
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
                resources.srcDirs("$buildDir/generated/src/iosMain/resources")
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

buildkonfig {
    packageName = "com.sdercolin.recstar"

    defaultConfigs {
        buildConfigField(FieldSpec.Type.BOOLEAN, "isDebug", "true")
        buildConfigField(FieldSpec.Type.STRING, "version", findProperty("app.versionName").toString())
        buildConfigField(FieldSpec.Type.INT, "versionCode", findProperty("app.versionCode").toString())
    }

    defaultConfigs("release") {
        buildConfigField(FieldSpec.Type.BOOLEAN, "isDebug", "false")
    }

    if (includeIos) {
        targetConfigs {
            create("iosX64") {
                buildConfigField(FieldSpec.Type.BOOLEAN, "isDebug", "false")
            }
            create("iosArm64") {
                buildConfigField(FieldSpec.Type.BOOLEAN, "isDebug", "false")
            }
            create("iosSimulatorArm64") {
                buildConfigField(FieldSpec.Type.BOOLEAN, "isDebug", "true")
            }
        }
    }
}

task("updateProjectVersions") {
    doLast {
        val versionName = findProperty("app.versionName")?.toString() ?: "0.1.0"
        val versionCode = findProperty("app.versionCode")?.toString()?.toInt() ?: 1
        val shellScriptFile = rootProject.file("tools/update_ios_version.sh")
        exec {
            commandLine("sh", shellScriptFile.absolutePath, versionName, versionCode)
        }
    }
}

if (includeIos) {
    licenseReport {
        renderers = arrayOf(JsonReportRenderer())
        configurations = arrayOf("iosMainResolvableDependenciesMetadata")
        excludes = arrayOf("org.jetbrains.skiko:skiko")
    }

    listOf(
        "iosX64",
        "iosArm64",
        "iosSimulatorArm64",
    ).forEach { target ->
        tasks.findByName("compileKotlin${target.uppercaseFirstChar()}")?.apply {
            dependsOn("generateLicenseReport")
            doLast {
                val generated = File("$buildDir/reports/dependency-license/index.json")
                val source = File("$buildDir/generated/src/iosMain/resources/license-report.json")
                generated.copyTo(source, overwrite = true)
            }
        }
    }
}
