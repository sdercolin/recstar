package ui.screen.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import ui.model.Screen
import ui.style.LocalThemeIsDarkMode
import ui.style.appDarkColors
import ui.style.appLightColors

object ThemeColorDemoScreen : Screen {
    @Composable
    override fun getTitle(): String = "Theme Color Demo"

    @Composable
    override fun Content() = ThemeDemo()
}

class ThemeColorDemoScreenModel : ScreenModel {
    enum class Theme {
        System,
        Light,
        Dark,
        Default,
    }

    var theme by mutableStateOf(Theme.Dark)
}

@Composable
private fun Screen.ThemeDemo() {
    val model = rememberScreenModel { ThemeColorDemoScreenModel() }
    val isDarkMode = LocalThemeIsDarkMode.current
    val colors = remember(model.theme) {
        when (model.theme) {
            ThemeColorDemoScreenModel.Theme.System -> if (isDarkMode) {
                appDarkColors
            } else {
                appLightColors
            }
            ThemeColorDemoScreenModel.Theme.Light -> appLightColors
            ThemeColorDemoScreenModel.Theme.Dark -> appDarkColors
            ThemeColorDemoScreenModel.Theme.Default -> darkColors()
        }
    }
    MaterialTheme(colors = colors) {
        ThemeDemoContent(
            theme = model.theme,
            setTheme = { model.theme = it },
        )
    }
}

@Composable
private fun ThemeDemoContent(
    theme: ThemeColorDemoScreenModel.Theme,
    setTheme: (ThemeColorDemoScreenModel.Theme) -> Unit,
) {
    Surface {
        Column(Modifier.fillMaxSize(), horizontalAlignment = CenterHorizontally) {
            Text(
                "Surface",
                modifier = Modifier.padding(20.dp),
            )
            Column(
                Modifier.padding(20.dp)
                    .fillMaxWidth()
                    .weight(1f)
                    .background(color = MaterialTheme.colors.background)
                    .border(1.dp, color = Color.White)
                    .border(1.dp, color = Color.Black),
                horizontalAlignment = CenterHorizontally,
            ) {
                Text(
                    "Background",
                    modifier = Modifier.padding(20.dp),
                    color = MaterialTheme.colors.onBackground,
                )
                Row(
                    Modifier.padding(20.dp)
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    Column(
                        Modifier.fillMaxHeight(),
                        horizontalAlignment = CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Box {
                            Row {
                                Box(Modifier.size(100.dp).background(color = MaterialTheme.colors.primary))
                                Box(Modifier.size(100.dp).background(color = MaterialTheme.colors.primaryVariant))
                            }
                            Text(
                                "Primary",
                                modifier = Modifier.align(Center),
                                color = MaterialTheme.colors.onPrimary,
                            )
                        }
                        Box {
                            Row {
                                Box(Modifier.size(100.dp).background(color = MaterialTheme.colors.secondary))
                                Box(Modifier.size(100.dp).background(color = MaterialTheme.colors.secondaryVariant))
                            }
                            Text(
                                "Secondary",
                                modifier = Modifier.align(Center),
                                color = MaterialTheme.colors.onSecondary,
                            )
                        }
                        Box(Modifier.size(200.dp, 100.dp).background(color = MaterialTheme.colors.error)) {
                            Text(
                                "Error",
                                modifier = Modifier.align(Center),
                                color = MaterialTheme.colors.onError,
                            )
                        }
                    }
                    Column(
                        Modifier.fillMaxHeight().width(IntrinsicSize.Max),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        ThemeColorDemoScreenModel.Theme.entries.forEach {
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = if (it == theme) {
                                        MaterialTheme.colors.secondary
                                    } else {
                                        MaterialTheme.colors.primary
                                    },
                                ),
                                onClick = { setTheme(it) },
                            ) {
                                Text(it.name)
                            }
                        }
                    }
                }
            }
        }
    }
}
