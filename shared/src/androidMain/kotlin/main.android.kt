import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import ui.model.AndroidContext
import ui.model.LocalScreenOrientation
import ui.model.ScreenOrientation
import java.lang.ref.WeakReference

@Composable
fun MainView() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val androidContext = remember(context) { AndroidContext(WeakReference(context), coroutineScope) }
    val orientation = when (LocalConfiguration.current.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> ScreenOrientation.Landscape
        Configuration.ORIENTATION_PORTRAIT -> ScreenOrientation.Portrait
        else -> ScreenOrientation.Undefined
    }
    CompositionLocalProvider(LocalScreenOrientation provides orientation) {
        App(androidContext)
    }
}
