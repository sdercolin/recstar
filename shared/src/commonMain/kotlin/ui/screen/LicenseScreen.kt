package ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import model.LicenseReport
import model.getLicenseReport
import ui.common.ScrollableLazyColumn
import ui.model.LocalAppContext
import ui.model.Screen
import ui.string.*
import util.Browser

object LicenseScreen : Screen {
    @Composable
    override fun getTitle(): String = string(Strings.LicenseScreenTitle)

    @Composable
    override fun Content() {
        val context = LocalAppContext.current
        val licenseReport = produceState<LicenseReport?>(initialValue = null) {
            value = getLicenseReport(context)
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            ScrollableLazyColumn(
                modifier = Modifier.fillMaxSize(),
            ) {
                items(licenseReport.value?.dependencies.orEmpty()) { dependency ->
                    Item(dependency)
                }
            }
            if (licenseReport.value == null) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun Item(dependency: LicenseReport.Dependency) {
    Column(modifier = Modifier.padding(horizontal = 30.dp, vertical = 15.dp)) {
        Text(
            text = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.body2.fontSize,
                        color = MaterialTheme.typography.body2.color,
                    ),
                ) {
                    append(dependency.moduleName)
                }
                withStyle(
                    SpanStyle(
                        fontSize = MaterialTheme.typography.caption.fontSize,
                        color = MaterialTheme.typography.caption.color,
                    ),
                ) {
                    append("  ${dependency.moduleVersion}")
                }
            },
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
        val license = dependency.moduleLicense
        val moduleUrl = dependency.moduleUrl
        if (license != null || moduleUrl != null) {
            Spacer(modifier = Modifier.height(5.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.Bottom) {
                if (license != null) {
                    val url = dependency.moduleLicenseUrl
                    if (url == null) {
                        Text(
                            text = license,
                            style = MaterialTheme.typography.caption,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    } else {
                        Link(
                            text = license,
                            url = url,
                        )
                    }
                }
                if (moduleUrl != null) {
                    Link(
                        text = "Website",
                        url = moduleUrl,
                    )
                }
            }
        }
    }
}

@Composable
private fun Link(
    text: String,
    url: String,
) {
    val context = LocalAppContext.current
    ClickableText(
        text = buildAnnotatedString {
            withStyle(
                SpanStyle(
                    color = MaterialTheme.colors.primary,
                    textDecoration = TextDecoration.Underline,
                ),
            ) {
                append(text)
            }
        },
        onClick = { Browser.openUrl(context, url) },
        style = MaterialTheme.typography.caption,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
