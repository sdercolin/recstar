package repository

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.staticCompositionLocalOf
import io.Paths
import io.appPreferenceFile
import model.AppPreference
import util.parseJson
import util.stringifyJson

/**
 * A repository to manage the app preference.
 */
class AppPreferenceRepository(appPreference: AppPreference) {
    private val _state = mutableStateOf(appPreference)
    val state: State<AppPreference> get() = _state
    val value: AppPreference get() = _state.value

    fun update(updater: AppPreference.() -> AppPreference) {
        val newValue = updater(value)
        _state.value = newValue
        Paths.appPreferenceFile.writeText(newValue.stringifyJson())
    }
}

fun createAppPreferenceRepository(): AppPreferenceRepository {
    val preferenceText = Paths.appPreferenceFile.takeIf { it.exists() }?.readText()
    val appPreference = preferenceText?.parseJson() ?: AppPreference().also {
        Paths.appPreferenceFile.writeText(it.stringifyJson())
    }
    return AppPreferenceRepository(appPreference)
}

val LocalAppPreferenceRepository = staticCompositionLocalOf<AppPreferenceRepository> {
    error("No AppPreferenceRepository provided")
}
