package ui.screen

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import io.Paths
import ui.common.LocalAlertDialogController
import ui.model.LocalAppContext
import ui.model.Screen

data class SessionScreen(val name: String) : Screen {
    override val title: String
        get() = name

    @Composable
    override fun Content() = SessionScreenContent()
}

private val dummyList = listOf(
    "Line 1",
    "Line 2",
    "Line 3",
    "Line 4",
    "Line 5",
)

private val dummyContentDirectory get() = Paths.contentRoot.resolve("Dummy Session")

@Composable
private fun Screen.SessionScreenContent() {
    val context = LocalAppContext.current
    val alertDialogController = LocalAlertDialogController.current
    val model =
        rememberScreenModel {
            SessionScreenModel(
                sentences = dummyList,
                contentDirectory = dummyContentDirectory,
                context = context,
                alertDialogController = alertDialogController,
            )
        }
}
