package model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ui.model.AppContext
import util.parseJson

actual suspend fun getLicenseReport(context: AppContext): LicenseReport? =
    withContext(Dispatchers.IO) {
        Thread.currentThread().contextClassLoader.getResource("license-report.json")?.openStream()?.use {
            it.reader().readText().parseJson<LicenseReport>()
        }?.let { it.copy(dependencies = it.dependencies + vendoredNativeDependencies) }
    }

private val vendoredNativeDependencies = listOf(
    LicenseReport.Dependency(
        moduleName = "miniaudio",
        moduleVersion = "0.11.25",
        moduleUrl = "https://miniaud.io",
        moduleLicense = "Public Domain (Unlicense) or MIT No Attribution",
        moduleLicenseUrl = "https://github.com/mackron/miniaudio/blob/master/LICENSE",
    ),
)
