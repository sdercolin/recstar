import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.Paths

private const val TEST_FILE_CONTENT = "aAあア亜啊🍣"
private val testExternalFile get() = Paths.contentRoot.resolve("test-external.txt")
private val testInternalFile get() = Paths.appRoot.resolve("test-internal.txt")

@Composable
fun App() {
    remember { ensurePaths() }
    MaterialTheme {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            listOf(
                "internal" to testInternalFile,
                "external" to testExternalFile
            ).forEach { (type, file) ->
                Column(
                    Modifier.weight(1f).fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    var fileExists by remember { mutableStateOf(file.exists()) }
                    var fileContent by remember { mutableStateOf("") }
                    var fileAbsolutePath by remember {
                        mutableStateOf(if (file.exists()) file.absolutePath else "")
                    }
                    Button(onClick = {
                        file.writeText(TEST_FILE_CONTENT)
                        fileExists = file.exists()
                        fileAbsolutePath = file.absolutePath
                    }) {
                        Text("Write to $type file")
                    }
                    Button(onClick = {
                        fileContent = file.readText()
                    }) {
                        Text("Read from $type file")
                    }
                    Text("$type file exists: $fileExists")
                    Text("$type file content: $fileContent")
                    Text("$type file absolute path: $fileAbsolutePath")
                }
            }
        }
    }
}

expect fun getPlatformName(): String

fun ensurePaths() {
    listOf(Paths.appRoot, Paths.contentRoot).forEach {
        if (!it.exists()) {
            it.mkdirs()
        }
    }
}