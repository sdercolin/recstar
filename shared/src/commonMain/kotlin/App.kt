import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import io.Paths
import ui.demo.OrientationDemo
import ui.model.AppContext
import ui.model.LocalAppContext
import util.Log

@Composable
fun App(context: AppContext) {
    CompositionLocalProvider(LocalAppContext provides context) {
        remember { ensurePaths() }
        MaterialTheme {
            OrientationDemo()
        }
    }
}

expect fun getPlatformName(): String

fun ensurePaths() {
    listOf(Paths.appRoot, Paths.contentRoot).forEach {
        Log.i("ensurePaths: ${it.absolutePath}")
        if (!it.exists()) {
            Log.i("not exists, creating...")
            val created = it.mkdirs()
            Log.i("created: $created")
        }
    }
}
