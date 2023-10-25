import androidx.compose.runtime.remember
import androidx.compose.ui.interop.LocalUIViewController
import androidx.compose.ui.window.ComposeUIViewController
import ui.model.ProvideScreenOrientation
import ui.model.ViewControllerContext

actual fun getPlatformName(): String = "iOS"

fun MainViewController() = ComposeUIViewController {
    val localUIViewController = LocalUIViewController.current
    val viewControllerContext = remember(localUIViewController) { ViewControllerContext(localUIViewController) }
    ProvideScreenOrientation {
        App(viewControllerContext)
    }
}
