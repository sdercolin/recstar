import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import io.LocalFileInteractor
import ui.common.OpenFileDialog
import ui.common.OpenFileDialogRequest
import ui.common.SaveFileDialog
import ui.common.SaveFileDialogRequest
import ui.model.LocalScreenOrientation
import ui.model.ScreenOrientation

@Composable
fun MainView() {
    val fileInteractor = LocalFileInteractor.current
    CompositionLocalProvider(LocalScreenOrientation provides ScreenOrientation.Landscape) {
        App()
        fileInteractor.fileDialogRequest?.let {
            when (it) {
                is OpenFileDialogRequest -> OpenFileDialog(it)
                is SaveFileDialogRequest -> SaveFileDialog(it)
            }
        }
    }
}
