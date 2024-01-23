package ui.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGFloat
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIEdgeInsets
import platform.UIKit.UIScreen
import platform.UIKit.UIViewController
import util.Log

@Composable
fun ProvideSafeAreaInsets(
    viewController: UIViewController,
    content: @Composable () -> Unit,
) {
    var safeAreaInsets by remember { mutableStateOf(getInitialSafeAreaInsets(viewController)) }
    DisposableEffect(Unit) {
        val cleanup = observeSafeAreaInsetsChanges {
            Log.i("Safe area insets changed: $it")
            safeAreaInsets = it
        }
        onDispose {
            cleanup()
        }
    }
    CompositionLocalProvider(LocalSafeAreaInsets provides safeAreaInsets) {
        content()
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun getInitialSafeAreaInsets(viewController: UIViewController): SafeAreaInsets {
    val view = viewController.view
    val safeAreaInsets = view.safeAreaInsets
    return safeAreaInsets.useContents { toSafeAreaInsets() }.also { insets ->
        Log.i("Initial safe area insets: $insets")
    }
}

private fun observeSafeAreaInsetsChanges(onChange: (SafeAreaInsets) -> Unit): () -> Unit {
    val notificationCenter = NSNotificationCenter.defaultCenter

    val observer = notificationCenter.addObserverForName(
        "SafeAreaDidChange",
        null,
        NSOperationQueue.mainQueue,
    ) { notification ->
        val insets = notification?.userInfo?.get("insets")
        (insets as? CGSafeAreaInsets)?.let { onChange(it.toSafeAreaInsets()) }
    }

    return {
        notificationCenter.removeObserver(observer)
    }
}

private fun UIEdgeInsets.toSafeAreaInsets(): SafeAreaInsets {
    val scale = UIScreen.mainScreen.scale.toFloat()
    return SafeAreaInsets(
        top = top.toFloat() * scale,
        left = left.toFloat() * scale,
        bottom = bottom.toFloat() * scale,
        right = right.toFloat() * scale,
    )
}

data class CGSafeAreaInsets(
    val top: CGFloat,
    val left: CGFloat,
    val bottom: CGFloat,
    val right: CGFloat,
) {
    fun toSafeAreaInsets(): SafeAreaInsets {
        val scale = UIScreen.mainScreen.scale.toFloat()
        return SafeAreaInsets(
            top = top.toFloat() * scale,
            left = left.toFloat() * scale,
            bottom = bottom.toFloat() * scale,
            right = right.toFloat() * scale,
        )
    }
}
