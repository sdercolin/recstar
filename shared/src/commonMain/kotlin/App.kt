import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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

private const val TEST_FILE_PATH = "test.txt"
private const val TEST_FILE_CONTENT = "aAあア亜啊🍣"
private val testFile get() = Paths.appRoot.resolve(TEST_FILE_PATH)

@Composable
fun App() {
    remember { ensurePaths() }
    MaterialTheme {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            var fileExists by remember { mutableStateOf(false) }
            var fileContent by remember { mutableStateOf("") }
            var fileAbsolutePath by remember { mutableStateOf("") }
            Button(onClick = {
                testFile.writeText(TEST_FILE_CONTENT)
                fileExists = testFile.exists()
                fileAbsolutePath = testFile.absolutePath
            }) {
                Text("Write to file")
            }
            Button(onClick = {
                fileContent = testFile.readText()
            }) {
                Text("Read from file")
            }
            Text("File exists: $fileExists")
            Text("File content: $fileContent")
            Text("File absolute path: $fileAbsolutePath")
        }
    }
}

expect fun getPlatformName(): String

fun ensurePaths() {
    listOf(Paths.appRoot).forEach {
        if (!it.exists()) {
            it.mkdirs()
        }
    }
}