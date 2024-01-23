import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.interop.LocalUIViewController
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import platform.Foundation.setValue
import platform.UIKit.UIDevice
import platform.UIKit.UIInterfaceOrientationUnknown
import platform.UIKit.UIViewController
import platform.UIKit.attemptRotationToDeviceOrientation
import ui.model.ProvideSafeAreaInsets
import ui.model.ProvideScreenOrientation
import ui.model.ViewControllerContext
import ui.model.requestedScreenOrientation

fun MainViewController() =
    ComposeUIViewController {
        val coroutineScope = rememberCoroutineScope()
        val viewController = LocalUIViewController.current
        val context = remember(viewController) { ViewControllerContext(viewController, coroutineScope) }
        val dependencies = remember(context) { AppDependencies(context) }
        LaunchedEffect(context) {
            dependencies.appPreferenceRepository.flow.map { it.orientation }.distinctUntilChanged().collectLatest {
                requestedScreenOrientation = it
                UIDevice.currentDevice.setValue(UIInterfaceOrientationUnknown, forKey = "orientation")
                UIViewController.attemptRotationToDeviceOrientation()
            }
        }
        ProvideAppDependencies(dependencies) {
            ProvideScreenOrientation {
                ProvideSafeAreaInsets(viewController) {
                    App()
                }
            }
        }
    }
