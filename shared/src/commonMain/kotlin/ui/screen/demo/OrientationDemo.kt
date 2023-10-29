package ui.screen.demo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ui.model.LocalScreenOrientation
import ui.model.Screen

object OrientationDemoScreen : Screen {
    @Composable
    override fun getTitle(): String = "Orientation Demo"

    @Composable
    override fun Content() = OrientationDemo()
}

@Composable
private fun OrientationDemo() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "CurrentOrientation: ${LocalScreenOrientation.current}")
    }
}
