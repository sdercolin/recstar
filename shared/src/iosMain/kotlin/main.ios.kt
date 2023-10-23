import androidx.compose.ui.interop.LocalUIViewController
import androidx.compose.ui.window.ComposeUIViewController
import ui.model.ViewControllerContext

actual fun getPlatformName(): String = "iOS"

fun MainViewController() = ComposeUIViewController {
    val localUIViewController = LocalUIViewController.current
    App(ViewControllerContext(localUIViewController))
}
