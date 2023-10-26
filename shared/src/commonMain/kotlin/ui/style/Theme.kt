package ui.style

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val appLightColors = lightColors(
    primary = Color(0xffe65100), // Orange 900
    primaryVariant = Color(0xffbf360c), // Deep Orange 900
    secondary = Color(0xfffdd835), // Yellow 600
    secondaryVariant = Color(0xffff8f00), // Amber 800
    surface = Color(0xffEEEEEE), // Grey 200
    background = Color(0xfff5f5f5), // Grey 100
    error = Color(0xffd32f2f), // Red 700
)

val appDarkColors = darkColors(
    primary = Color(0xfff9a825), // Yellow 800
    primaryVariant = Color(0xffe65100), // Orange 900
    secondary = Color(0xfffdd835), // Yellow 600
    secondaryVariant = Color(0xfffdd835), // Yellow 600
    surface = Color(0xff252525), // Grey 800~900
    background = Color(0xff1e1e1e), // Grey 900++
    error = Color(0xfff44336), // Red 500
)

@Composable
fun AppTheme(
    isDarkMode: Boolean,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colors = if (isDarkMode) appDarkColors else appLightColors,
        content = content,
    )
}
