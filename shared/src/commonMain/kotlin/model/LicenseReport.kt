package model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import ui.model.AppContext

/**
 * A serializable report of Open Source Licenses used in this project.
 *
 * @property dependencies The list of dependencies.
 */
@Serializable
@Immutable
data class LicenseReport(
    val dependencies: List<Dependency>,
) {
    @Serializable
    @Immutable
    data class Dependency(
        val moduleName: String,
        val moduleVersion: String,
        val moduleUrl: String? = null,
        val moduleLicense: String? = null,
        val moduleLicenseUrl: String? = null,
    )
}

expect suspend fun getLicenseReport(context: AppContext): LicenseReport?
