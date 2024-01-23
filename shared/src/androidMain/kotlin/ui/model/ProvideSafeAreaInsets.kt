package ui.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState

@Composable
fun ProvideSafeAreaInsets(
    context: AndroidContext,
    content: @Composable () -> Unit,
) {
    val safeAreaInsets = context.safeAreaInsetsFlow.collectAsState()
    CompositionLocalProvider(LocalSafeAreaInsets provides safeAreaInsets.value) {
        content()
    }
}
