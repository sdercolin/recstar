import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import io.LocalFileInteractor
import model.AppPreference
import repository.LocalAppPreferenceRepository
import ui.common.OpenFileDialog
import ui.common.OpenFileDialogRequest
import ui.common.SaveFileDialog
import ui.common.SaveFileDialogRequest
import ui.model.LocalScreenOrientation
import ui.model.ScreenOrientation

@Composable
fun MainView() {
    val fileInteractor = LocalFileInteractor.current
    val appPreference by LocalAppPreferenceRepository.current.flow.collectAsState()
    val orientation by derivedStateOf {
        when (appPreference.orientation) {
            AppPreference.ScreenOrientation.Portrait -> ScreenOrientation.Portrait
            else -> ScreenOrientation.Landscape
        }
    }
    CompositionLocalProvider(LocalScreenOrientation provides orientation) {
        App()
        fileInteractor.fileDialogRequest?.let {
            when (it) {
                is OpenFileDialogRequest -> OpenFileDialog(it)
                is SaveFileDialogRequest -> SaveFileDialog(it)
            }
        }
    }
}
