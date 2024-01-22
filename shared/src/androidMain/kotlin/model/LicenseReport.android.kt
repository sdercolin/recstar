package model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ui.model.AppContext
import ui.model.androidNativeContext
import util.parseJson

actual suspend fun getLicenseReport(context: AppContext): LicenseReport? =
    withContext(Dispatchers.IO) {
        val androidContext = context.androidNativeContext
        val resId = licenseReportResId ?: return@withContext null
        val text = androidContext.resources.openRawResource(resId).bufferedReader().use { it.readText() }
        text.parseJson()
    }

var licenseReportResId: Int? = null
