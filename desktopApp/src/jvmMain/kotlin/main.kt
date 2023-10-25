import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import util.Log

fun main() =
    application {
        remember {
            Log.initialize(enableSystemOut = true)
        }

        Window(onCloseRequest = ::exitApplication) {
            MainView()
        }
    }
