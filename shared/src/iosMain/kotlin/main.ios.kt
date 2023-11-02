import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.interop.LocalUIViewController
import androidx.compose.ui.window.ComposeUIViewController
import ui.model.ProvideScreenOrientation
import ui.model.ViewControllerContext

fun MainViewController() =
    ComposeUIViewController {
        val coroutineScope = rememberCoroutineScope()
        val viewController = LocalUIViewController.current
        val context = remember(viewController) { ViewControllerContext(viewController, coroutineScope) }
        val dependencies = remember(context) { AppDependencies(context) }
        ProvideAppDependencies(dependencies) {
            ProvideScreenOrientation {
                App()
            }
        }
    }
