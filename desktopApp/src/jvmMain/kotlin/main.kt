import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.ensurePaths
import util.Log

fun main() =
    application {
        remember {
            ensurePaths()
            Log.initialize()
        }

        Window(onCloseRequest = ::exitApplication) {
            MainView()
        }
    }
