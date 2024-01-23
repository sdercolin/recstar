import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import ui.model.LocalScreenOrientation
import ui.model.ProvideSafeAreaInsets
import ui.model.ScreenOrientation
import ui.model.androidContext

@Composable
fun MainView(dependencies: AppDependencies) {
    val orientation = when (LocalConfiguration.current.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> ScreenOrientation.Landscape
        Configuration.ORIENTATION_PORTRAIT -> ScreenOrientation.Portrait
        else -> ScreenOrientation.Undefined
    }
    ProvideAppDependencies(dependencies) {
        CompositionLocalProvider(LocalScreenOrientation provides orientation) {
            ProvideSafeAreaInsets(dependencies.context.androidContext) {
                App()
            }
        }
    }
}
