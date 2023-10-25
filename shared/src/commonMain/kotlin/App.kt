import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import io.Paths
import ui.common.AlertDialogController
import ui.common.LocalAlertDialogController
import ui.common.LocalToastController
import ui.common.ToastController
import ui.model.AppContext
import ui.model.LocalAppContext
import ui.model.Screen
import ui.screen.demo.DemoShowcaseScreen
import util.Log

@Composable
fun App(context: AppContext) {
    remember { ensurePaths() }

    val alertDialogController = remember(context) { AlertDialogController(context) }
    val toastController = remember(context) { ToastController(context) }
    CompositionLocalProvider(
        LocalAppContext provides context,
        LocalAlertDialogController provides alertDialogController,
        LocalToastController provides toastController,
    ) {
        MaterialTheme {
            Surface {
                Navigator(DemoShowcaseScreen) { navigator ->
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = {
                                    val currentTitle = (navigator.lastItem as Screen).title
                                    Text(text = "RecStar - $currentTitle")
                                },
                                navigationIcon = if (navigator.size > 1) {
                                    @Composable {
                                        IconButton(onClick = { navigator.pop() }) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowBack,
                                                contentDescription = "Back",
                                            )
                                        }
                                    }
                                } else {
                                    null
                                },
                            )
                        },
                        content = {
                            SlideTransition(navigator) { screen -> screen.Content() }
                        },
                    )
                }
                alertDialogController.Compose()
                toastController.Compose()
            }
        }
    }
}

private fun ensurePaths() {
    listOf(Paths.appRoot, Paths.contentRoot).forEach {
        Log.i("ensurePaths: ${it.absolutePath}")
        if (!it.exists()) {
            Log.i("not exists, creating...")
            val created = it.mkdirs()
            Log.i("created: $created")
        }
    }
}
