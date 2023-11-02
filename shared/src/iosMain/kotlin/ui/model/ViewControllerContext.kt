package ui.model

import kotlinx.coroutines.CoroutineScope
import platform.UIKit.UIViewController

class ViewControllerContext(
    val uiViewController: UIViewController,
    override val coroutineScope: CoroutineScope,
) : AppContext

val AppContext.uiViewControllerContext: ViewControllerContext
    get() = this as ViewControllerContext
