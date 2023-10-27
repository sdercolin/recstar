import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
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
import ui.common.AlertDialogController
import ui.common.LocalAlertDialogController
import ui.common.LocalToastController
import ui.common.ToastController
import ui.model.AppContext
import ui.model.LocalAppContext
import ui.model.Screen
import ui.screen.demo.DemoShowcaseScreen
import ui.string.Strings
import ui.string.string
import ui.style.AppTheme

@Composable
fun App(context: AppContext) {
    val alertDialogController = remember(context) { AlertDialogController(context) }
    val toastController = remember(context) { ToastController(context) }
    CompositionLocalProvider(
        LocalAppContext provides context,
        LocalAlertDialogController provides alertDialogController,
        LocalToastController provides toastController,
    ) {
        AppTheme(isSystemInDarkTheme()) {
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
                                                contentDescription = string(Strings.CommonBack),
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
