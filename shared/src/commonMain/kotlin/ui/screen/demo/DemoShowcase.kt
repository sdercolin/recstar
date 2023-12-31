package ui.screen.demo

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
import ui.model.Screen

object DemoShowcaseScreen : Screen {
    @Composable
    override fun getTitle(): String = "Demo Showcase"

    @Composable
    override fun Content() = DemoShowcase()
}

@Composable
private fun DemoShowcase() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            DemoButton("File System Demo") { FileSystemDemoScreen }
            DemoButton("Orientation Demo") { OrientationDemoScreen }
            DemoButton("Recorder Demo") { RecorderDemoScreen }
            DemoButton("Alert Demo") { AlertDemoScreen }
            DemoButton("Theme Color Demo") { ThemeColorDemoScreen }
            DemoButton("Theme Typography Demo") { ThemeTypographyDemoScreen }
            DemoButton("Exception Demo") { ExceptionDemoScreen }
        }
    }
}

@Composable
private fun DemoButton(
    text: String,
    target: () -> Screen,
) {
    val navigator = LocalNavigator.currentOrThrow
    Button(onClick = { navigator push target() }) {
        Text(text)
    }
}
