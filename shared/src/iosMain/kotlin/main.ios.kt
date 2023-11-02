import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.interop.LocalUIViewController
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.cinterop.BetaInteropApi
import ui.model.ProvideScreenOrientation
import ui.model.ViewControllerContext

@OptIn(BetaInteropApi::class)
fun MainViewController() =
    ComposeUIViewController {
        val coroutineScope = rememberCoroutineScope()
        val viewController = LocalUIViewController.current
        val viewControllerContext = remember(viewController) { ViewControllerContext(viewController, coroutineScope) }
        ProvideScreenOrientation {
            App(viewControllerContext)
        }
    }
