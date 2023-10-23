import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import io.Paths
import ui.RecorderDemo
import ui.model.AppContext
import ui.model.LocalAppContext


@Composable
fun App(context: AppContext) {
    CompositionLocalProvider(LocalAppContext provides context) {
        remember { ensurePaths() }
        MaterialTheme {
            //FileSystemDemo()
            RecorderDemo()
        }
    }
}

expect fun getPlatformName(): String

fun ensurePaths() {
    listOf(Paths.appRoot, Paths.contentRoot).forEach {
        println("ensurePaths: ${it.absolutePath}")
        if (!it.exists()) {
            println("not exists, creating...")
            val created = it.mkdirs()
            println("created: $created")
        }
    }
}
