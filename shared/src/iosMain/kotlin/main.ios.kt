import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.interop.LocalUIViewController
import androidx.compose.ui.window.ComposeUIViewController
import ui.model.ProvideScreenOrientation
import ui.model.ViewControllerContext

fun MainViewController() = ComposeUIViewController {
    val localUIViewController = LocalUIViewController.current
    val coroutineScope = rememberCoroutineScope()
    val viewControllerContext = remember(localUIViewController) {
        ViewControllerContext(localUIViewController, coroutineScope)
    }
    ProvideScreenOrientation {
        App(viewControllerContext)
    }
}
