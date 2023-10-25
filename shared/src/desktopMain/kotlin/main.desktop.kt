import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import ui.model.DesktopContext
import ui.model.LocalScreenOrientation
import ui.model.ScreenOrientation

@Composable
fun MainView() {
    val coroutineScope = rememberCoroutineScope()
    CompositionLocalProvider(LocalScreenOrientation provides ScreenOrientation.Landscape) {
        // Currently always use landscape orientation on desktop
        // Later we could use the window size or some settings to determine the orientation
        App(DesktopContext(coroutineScope))
    }
}
