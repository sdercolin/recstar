import com.github.jk1.license.render.JsonReportRenderer
import org.gradle.kotlin.dsl.support.uppercaseFirstChar

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
        classpath("com.codingfeline.buildkonfig:buildkonfig-gradle-plugin:0.15.1")
    }
}

plugins {
    kotlin("multiplatform")
    id("com.android.application")
    id("org.jetbrains.compose")
    id("com.github.jk1.dependency-license-report") version "2.5"
}

kotlin {
    androidTarget()
    sourceSets {
        val androidMain by getting {
            dependencies {
                implementation(project(":shared"))
                implementation("androidx.activity:activity-ktx:1.8.2")
                implementation("androidx.fragment:fragment-ktx:1.6.2")
            }
        }
    }
}

android {
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    namespace = "com.sdercolin.recstar"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

    defaultConfig {
        applicationId = "com.sdercolin.recstar"
        minSdk = (findProperty("android.minSdk") as String).toInt()
        targetSdk = (findProperty("android.targetSdk") as String).toInt()
        versionCode = findProperty("app.versionCode")?.toString()?.toInt() ?: 1
        versionName = findProperty("app.versionName")?.toString() ?: "0.1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
    lint {
        checkReleaseBuilds = false
    }
}

licenseReport {
    renderers = arrayOf(JsonReportRenderer())
}
android.applicationVariants.configureEach {
    val variant = this
    val variantCapital = variant.name.uppercaseFirstChar()

    val copyTask = tasks.register("copy${variantCapital}LicenseReport") {
        doLast {
            val generated = layout.buildDirectory.file("reports/dependency-license/index.json").get().asFile
            val resource = layout.buildDirectory.dir("generated/res/resValues/${variant.name}/raw/license_report.json")
                .get().asFile
            generated.copyTo(resource, overwrite = true)
        }
        dependsOn("generateLicenseReport")
    }

    tasks["merge${variantCapital}Resources"].dependsOn(copyTask)
    tasks["package${variantCapital}Resources"].dependsOn(copyTask)
}
