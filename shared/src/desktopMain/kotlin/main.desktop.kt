import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import ui.model.DesktopContext

actual fun getPlatformName(): String = "Desktop"

@Composable
fun MainView() = App(DesktopContext())

@Preview
@Composable
fun AppPreview() {
    App(DesktopContext())
}