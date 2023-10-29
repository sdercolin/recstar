package ui.style

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Light color palette for the app.
 * - Primary: Orange 900
 * - Primary variant: Deep Orange 900
 * - Secondary: Yellow 600
 * - Secondary variant: Amber 800
 * - Surface: Grey 200
 * - Background: Grey 100
 * - Error: Red 700
 */
val appLightColors = lightColors(
    primary = Color(0xffe65100),
    primaryVariant = Color(0xffbf360c),
    secondary = Color(0xfffdd835),
    secondaryVariant = Color(0xffff8f00),
    surface = Color(0xffEEEEEE),
    background = Color(0xfff5f5f5),
    error = Color(0xffd32f2f),
)

/**
 * Dark color palette for the app.
 * - Primary: Yellow 800
 * - Primary variant: Orange 900
 * - Secondary: Yellow 600
 * - Secondary variant: Yellow 600
 * - Surface: between Grey 800 and 900
 * - Background: larger than Grey 900
 */
val appDarkColors = darkColors(
    primary = Color(0xfff9a825),
    primaryVariant = Color(0xffe65100),
    secondary = Color(0xfffdd835),
    secondaryVariant = Color(0xfffdd835),
    surface = Color(0xff252525),
    background = Color(0xff1e1e1e),
    error = Color(0xfff44336),
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