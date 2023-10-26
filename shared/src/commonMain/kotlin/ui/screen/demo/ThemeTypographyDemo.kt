package ui.screen.demo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ui.common.ScrollableColumn
import ui.model.Screen

object ThemeTypographyDemoScreen : Screen {
    override val title: String
        get() = "Theme Typography Demo"

    @Composable
    override fun Content() {
        ThemeTypographyDemoContent()
    }
}

@Composable
private fun ThemeTypographyDemoContent() {
    ScrollableColumn(
        modifier = Modifier.fillMaxSize().padding(30.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Text("h1 Aaあア亜啊", style = MaterialTheme.typography.h1)
        Text("h2 Aaあア亜啊", style = MaterialTheme.typography.h2)
        Text("h3 Aaあア亜啊", style = MaterialTheme.typography.h3)
        Text("h4 Aaあア亜啊", style = MaterialTheme.typography.h4)
        Text("h5 Aaあア亜啊", style = MaterialTheme.typography.h5)
        Text("h6 Aaあア亜啊", style = MaterialTheme.typography.h6)
        Text("subtitle1 Aaあア亜啊", style = MaterialTheme.typography.subtitle1)
        Text("subtitle2 Aaあア亜啊", style = MaterialTheme.typography.subtitle2)
        Text("body1 Aaあア亜啊", style = MaterialTheme.typography.body1)
        Text("body2 Aaあア亜啊", style = MaterialTheme.typography.body2)
        Text("button Aaあア亜啊", style = MaterialTheme.typography.button)
        Text("caption Aaあア亜啊", style = MaterialTheme.typography.caption)
        Text("overline Aaあア亜啊", style = MaterialTheme.typography.overline)
    }
}
