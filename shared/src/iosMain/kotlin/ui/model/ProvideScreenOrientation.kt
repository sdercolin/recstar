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
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIDevice
import platform.UIKit.UIDeviceOrientation.UIDeviceOrientationLandscapeLeft
import platform.UIKit.UIDeviceOrientation.UIDeviceOrientationLandscapeRight
import platform.UIKit.UIDeviceOrientation.UIDeviceOrientationPortrait
import platform.UIKit.UIDeviceOrientation.UIDeviceOrientationPortraitUpsideDown
import platform.UIKit.UIDeviceOrientationDidChangeNotification
import platform.UIKit.UIScreen
import util.Log

@Composable
fun ProvideScreenOrientation(content: @Composable () -> Unit) {
    var orientation by remember { mutableStateOf(getInitialOrientation()) }
    DisposableEffect(Unit) {
        val cleanup = observeOrientationChanges {
            Log.i("Screen orientation changed: $it")
            orientation = it
        }
        onDispose {
            cleanup()
        }
    }
    CompositionLocalProvider(LocalScreenOrientation provides orientation) {
        content()
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun getInitialOrientation(): ScreenOrientation {
    val bounds = UIScreen.mainScreen.bounds
    val width = bounds.useContents { this.size.width }
    val height = bounds.useContents { this.size.height }
    return when {
        width < height -> ScreenOrientation.Portrait
        width > height -> ScreenOrientation.Landscape
        else -> ScreenOrientation.Undefined
    }
}

private fun getCurrentOrientation(): ScreenOrientation = when (UIDevice.currentDevice.orientation) {
    UIDeviceOrientationPortrait -> ScreenOrientation.Portrait
    UIDeviceOrientationPortraitUpsideDown,
    UIDeviceOrientationLandscapeLeft,
    UIDeviceOrientationLandscapeRight,
    -> ScreenOrientation.Landscape
    else -> getInitialOrientation()
}

private fun observeOrientationChanges(onChange: (ScreenOrientation) -> Unit): () -> Unit {
    val notificationCenter = NSNotificationCenter.defaultCenter

    val observer = notificationCenter.addObserverForName(
        UIDeviceOrientationDidChangeNotification,
        null,
        NSOperationQueue.mainQueue,
    ) { _ ->
        Log.i(UIDevice.currentDevice.orientation.toString())
        onChange(getCurrentOrientation())
    }

    UIDevice.currentDevice.beginGeneratingDeviceOrientationNotifications()

    return {
        UIDevice.currentDevice.endGeneratingDeviceOrientationNotifications()
        notificationCenter.removeObserver(observer)
    }
}
