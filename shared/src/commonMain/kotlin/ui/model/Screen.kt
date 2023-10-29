package ui.model

import androidx.compose.runtime.Composable

interface Screen : cafe.adriel.voyager.core.screen.Screen {
    @Composable
    fun getTitle(): String
}
