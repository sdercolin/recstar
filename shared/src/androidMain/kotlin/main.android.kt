import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import ui.model.AndroidContext
import java.lang.ref.WeakReference

actual fun getPlatformName(): String = "Android"

@Composable
fun MainView() {
    val context = LocalContext.current
    val androidContext = remember { AndroidContext(WeakReference(context)) }
    App(androidContext)
}
