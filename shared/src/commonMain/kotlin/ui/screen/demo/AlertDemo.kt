package ui.screen.demo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ui.common.LocalAlertDialogController
import ui.common.LocalToastController
import ui.common.ToastDuration
import ui.common.requestConfirm
import ui.common.requestConfirmCancellable
import ui.common.requestYesNo
import ui.common.show
import ui.model.Screen

object AlertDemoScreen : Screen {
    @Composable
    override fun getTitle(): String = "Alert Demo"

    @Composable
    override fun Content() = AlertDemo()
}

@Composable
private fun AlertDemo() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val alertController = LocalAlertDialogController.current
            val toastController = LocalToastController.current
            var status by remember { mutableStateOf("Idle.") }
            Button(
                onClick = {
                    status = "Showing alert..."
                    alertController.requestConfirm(
                        message = "This is a message.",
                        onFinish = {
                            status = "Confirmed."
                        },
                    )
                },
            ) {
                Text("Show alert")
            }
            Button(
                onClick = {
                    status = "Showing alert..."
                    alertController.requestConfirmCancellable(
                        title = "Title",
                        message = "This is a message.",
                        onConfirm = {
                            status = "Positive button clicked."
                        },
                        onDismiss = {
                            status = "Negative button clicked."
                        },
                    )
                },
            ) {
                Text("Show alert with confirm/cancel buttons")
            }
            Button(
                onClick = {
                    status = "Showing alert..."
                    alertController.requestYesNo(
                        title = "Title",
                        message = "This is a message.",
                        onConfirm = {
                            status = "Positive button clicked."
                        },
                        onDismiss = {
                            status = "Negative button clicked."
                        },
                    )
                },
            ) {
                Text("Show alert with yes/no buttons")
            }
            Button(
                onClick = {
                    toastController.show("This is a toast.")
                },
            ) {
                Text("Show toast (short)")
            }
            Button(
                onClick = {
                    toastController.show("This is a toast.", duration = ToastDuration.Long)
                },
            ) {
                Text("Show toast (long)")
            }
            Text(status)
        }
    }
}
