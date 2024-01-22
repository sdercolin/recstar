package model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ui.model.AppContext
import util.parseJson

actual suspend fun getLicenseReport(context: AppContext): LicenseReport? =
    withContext(Dispatchers.IO) {
        Thread.currentThread().contextClassLoader.getResource("license-report.json")?.openStream()?.use {
            it.reader().readText().parseJson()
        }
    }
