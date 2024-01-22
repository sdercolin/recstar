package model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.resource
import ui.model.AppContext
import util.Log
import util.parseJson

@OptIn(ExperimentalResourceApi::class)
actual suspend fun getLicenseReport(context: AppContext): LicenseReport? =
    withContext(Dispatchers.IO) {
        try {
            val res = resource("license-report.json")
            res.readBytes().decodeToString().parseJson()
        } catch (e: Throwable) {
            Log.e(e)
            null
        }
    }
