import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import ui.model.DesktopContext
import ui.model.LocalScreenOrientation
import ui.model.ScreenOrientation

actual fun getPlatformName(): String = "Desktop"

@Composable
fun MainView() {
    CompositionLocalProvider(LocalScreenOrientation provides ScreenOrientation.Landscape) {
        // Currently always use landscape orientation on desktop
        // Later we could use the window size or some settings to determine the orientation
        App(DesktopContext())
    }
}

@Preview
@Composable
fun AppPreview() {
    App(DesktopContext())
}
