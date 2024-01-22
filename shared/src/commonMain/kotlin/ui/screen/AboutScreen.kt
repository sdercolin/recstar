package ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import ui.common.LocalToastController
import ui.common.ScrollableColumn
import ui.common.show
import ui.model.LocalAppContext
import ui.model.LocalScreenOrientation
import ui.model.Screen
import ui.model.ScreenOrientation
import ui.string.*
import util.Browser
import util.Clipboard
import util.Locale
import util.Platform
import util.appVersion
import util.isAndroid
import util.isDebug

@OptIn(ExperimentalResourceApi::class)
object AboutScreen : Screen {
    @Composable
    override fun getTitle(): String = string(Strings.PreferenceAbout)

    @Composable
    override fun Content() {
        val context = LocalAppContext.current
        val toastController = LocalToastController.current
        val navigator = LocalNavigator.currentOrThrow
        Box(
            modifier = Modifier.fillMaxSize().background(color = MaterialTheme.colors.background),
            contentAlignment = Alignment.Center,
        ) {
            val width = with(LocalDensity.current) {
                1024.toDp() // the banner's width
            }
            val isPortrait = LocalScreenOrientation.current == ScreenOrientation.Portrait
            ScrollableColumn(
                modifier = Modifier.fillMaxHeight().run {
                    if (isPortrait) fillMaxWidth() else this.width(width)
                },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painterResource("img/banner.png"),
                    null,
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.FillWidth,
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text("RecStar v$appVersion running on ${Platform.os}", style = MaterialTheme.typography.subtitle2)
                Spacer(modifier = Modifier.height(16.dp))
                Text("sdercolin @ 2023", style = MaterialTheme.typography.caption)
                Spacer(modifier = Modifier.height(16.dp))
                AboutButton(Strings.AboutScreenPrivacyPolicy) {
                    Browser.openUrl(context, PRIVACY_POLICY_URL)
                }
                AboutButton(Strings.AboutScreenCopyDeviceInfo) {
                    val info = """
                        App version: $appVersion
                        Target: ${Platform.target}
                        OS: ${Platform.os}
                        System locale: $Locale
                        Debug mode: $isDebug
                    """.trimIndent()
                    Clipboard.copy(context, info)
                    if (isAndroid.not()) {
                        // Newer versions of Android have a built-in toast message when copying.
                        toastController.show(stringStatic(Strings.AboutScreenDeviceInfoCopied))
                    }
                }
                AboutButton(Strings.AboutScreenViewLicenses) {
                    navigator push LicenseScreen
                }
                AboutButton(Strings.AboutScreenViewOnGithub) {
                    Browser.openUrl(context, GITHUB_URL)
                }
            }
        }
    }
}

@Composable
private fun AboutButton(
    textKey: Strings,
    onClick: () -> Unit,
) = Button(
    modifier = Modifier.padding(vertical = 8.dp).width(300.dp).height(48.dp),
    onClick = onClick,
) {
    Text(string(textKey))
}

private const val PRIVACY_POLICY_URL = "https://github.com/sdercolin/recstar/blob/main/privacy.md"
private const val GITHUB_URL = "https://github.com/sdercolin/recstar"
