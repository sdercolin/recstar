plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    kotlin("multiplatform").apply(false)
    id("com.android.application").apply(false)
    id("com.android.library").apply(false)
    id("org.jetbrains.compose").apply(false)
    id("org.jlleitschuh.gradle.ktlint") version "11.6.1"
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set("0.45.2")
        enableExperimentalRules.set(true)
        disabledRules.set(setOf("no-wildcard-imports", "filename"))
    }
}
