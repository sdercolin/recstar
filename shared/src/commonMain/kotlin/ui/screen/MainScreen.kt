package ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import const.APP_NAME
import ui.model.Screen
import ui.screen.demo.DemoShowcaseScreen

object MainScreen : Screen {
    @Composable
    override fun getTitle(): String = APP_NAME

    @Composable
    override fun Content() = MainScreenContent()
}

@Composable
private fun MainScreenContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            EntryButton("New Session") { CreateSessionReclistScreen }
            EntryButton("Feature Demos") { DemoShowcaseScreen }
        }
    }
}

@Composable
private fun EntryButton(
    text: String,
    target: () -> Screen,
) {
    val navigator = LocalNavigator.currentOrThrow
    Button(onClick = { navigator push target() }) {
        Text(text)
    }
}
