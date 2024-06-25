package repository

import androidx.compose.runtime.staticCompositionLocalOf
import io.Paths
import io.appPreferenceFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import model.AppPreference
import util.parseJson
import util.stringifyJson

/**
 * A repository to manage the app preference.
 */
class AppPreferenceRepository(appPreference: AppPreference) {
    private val _flow = MutableStateFlow(appPreference)
    val flow: StateFlow<AppPreference> get() = _flow
    val value: AppPreference get() = _flow.value

    fun update(updater: AppPreference.() -> AppPreference) {
        val newValue = updater(value)
        _flow.value = newValue
        Paths.appPreferenceFile.writeText(newValue.stringifyJson())
    }
}

fun createAppPreferenceRepository(): AppPreferenceRepository {
    val preferenceText = Paths.appPreferenceFile.takeIf { it.exists() }?.readText()
    val appPreference = preferenceText?.runCatching { parseJson<AppPreference>() }?.getOrNull()
        ?: AppPreference().also {
            Paths.appPreferenceFile.writeText(it.stringifyJson())
        }
    return AppPreferenceRepository(appPreference)
}

val LocalAppPreferenceRepository = staticCompositionLocalOf<AppPreferenceRepository> {
    error("No AppPreferenceRepository provided")
}
