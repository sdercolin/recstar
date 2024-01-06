package ui.screen.demo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.LocalFileInteractor
import io.Paths
import io.logsDirectory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ui.common.LocalToastController
import ui.common.show
import ui.model.LocalAppContext
import ui.model.Screen
import util.Log
import util.runCatchingCancellable

object ExceptionDemoScreen : Screen {
    @Composable
    override fun getTitle(): String = "ExceptionDemo"

    @Composable
    override fun Content() = ExceptionDemo()
}

@Composable
private fun ExceptionDemo() {
    val context = LocalAppContext.current
    val fileInteractor = LocalFileInteractor.current
    val toastController = LocalToastController.current
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(
            onClick = {
                throw Exception("Exception tested in composition")
            },
        ) {
            Text(text = "Throw Exception on UI Thread")
        }
        Button(
            onClick = {
                context.coroutineScope.launch {
                    throw Exception("Exception tested in default coroutine scope")
                }
            },
        ) {
            Text(text = "Throw Exception on Default Coroutine Scope")
        }
        Button(
            onClick = {
                context.coroutineScope.launch(Dispatchers.IO) {
                    throw Exception("Exception tested in IO coroutine scope")
                }
            },
        ) {
            Text(text = "Throw Exception on IO Coroutine Scope")
        }
        Button(
            onClick = {
                context.coroutineScope.launch(Dispatchers.IO) {
                    runCatching {
                        throw Exception("Caught exception tested in IO coroutine scope")
                    }.onFailure {
                        toastController.show(it.message ?: "")
                        Log.e(it)
                    }
                }
            },
        ) {
            Text(text = "Catch Exception on IO Coroutine Scope")
        }
        Button(
            onClick = {
                context.coroutineScope.launch {
                    val job = launch {
                        runCatchingCancellable {
                            delay(1000)
                        }.onFailure {
                            toastController.show(it.message ?: "")
                            Log.e(it)
                        }
                    }
                    delay(500)
                    job.cancel()
                }
            },
        ) {
            Text(text = "Ignore CancellationException Coroutine Scope")
        }
        Button(
            onClick = {
                context.coroutineScope.launch {
                    val job = launch {
                        runCatching {
                            delay(1000)
                        }.onFailure {
                            toastController.show(it.message ?: "")
                            Log.e(it)
                        }
                    }
                    delay(500)
                    job.cancel()
                }
            },
        ) {
            Text(text = "Catch CancellationException Coroutine Scope")
        }
        Button(
            onClick = {
                fileInteractor.requestOpenFolder(Paths.logsDirectory)
            },
        ) {
            Text("Open log position")
        }
    }
}
