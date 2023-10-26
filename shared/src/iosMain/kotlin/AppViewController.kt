import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.useContents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import platform.UIKit.UIGestureRecognizerStateBegan
import platform.UIKit.UIGestureRecognizerStateCancelled
import platform.UIKit.UIGestureRecognizerStateEnded
import platform.UIKit.UIPanGestureRecognizer
import platform.UIKit.UIViewController
import platform.UIKit.addChildViewController
import platform.UIKit.didMoveToParentViewController
import ui.model.ProvideScreenOrientation
import ui.model.ViewControllerContext

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class AppViewController : UIViewController(null, null) {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private val viewControllerContext = ViewControllerContext(this, coroutineScope)

    override fun viewDidLoad() {
        super.viewDidLoad()

        // The gesture recognizer is conflicting with compose scroll view, so we disable it for now.
        // val panGesture = UIPanGestureRecognizer(target = this, action = NSSelectorFromString("handlePan:"))
        // view.addGestureRecognizer(panGesture)

        val composeController = composeUIViewController()
        addChildViewController(composeController)
        composeController.view.setFrame(view.bounds)
        view.addSubview(composeController.view)
        composeController.didMoveToParentViewController(this)
    }

    override fun viewDidDisappear(animated: Boolean) {
        super.viewDidDisappear(animated)
        coroutineScope.cancel()
    }

    private fun composeUIViewController() =
        ComposeUIViewController {
            ProvideScreenOrientation {
                App(viewControllerContext)
            }
        }

    private var startedDetecting = false

    @ObjCAction
    fun handlePan(sender: UIPanGestureRecognizer) {
        val transitionX = sender.translationInView(view).useContents { x }
        val pointerX = sender.locationInView(view).useContents { x }
        val width = view.frame.useContents { size.width }
        when (sender.state) {
            UIGestureRecognizerStateBegan -> {
                if (pointerX < width / 6) {
                    startedDetecting = true
                }
            }
            UIGestureRecognizerStateEnded, UIGestureRecognizerStateCancelled -> {
                if (startedDetecting) {
                    if (transitionX > width / 3) {
                        postPopRequest()
                    }
                    startedDetecting = false
                }
            }
            else -> return
        }
    }

    private fun postPopRequest() {
        coroutineScope.launch {
            viewControllerContext.postNavigationPopEvent()
        }
    }
}
