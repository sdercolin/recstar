package util

import androidx.compose.ui.text.toLowerCase
import java.io.BufferedReader
import java.io.InputStreamReader

val osName by lazy { System.getProperty("os.name").toString() }
val osNameWithVersion by lazy { osName + " " + System.getProperty("os.version") }
val osRawArch by lazy { System.getProperty("os.arch").toString() }
val osArch by lazy {
    val arch = osRawArch
    if (isMacOS) {
        when {
            isRunningOnRosetta -> "x86_64 (Rosetta)"
            isMacOSWithArm -> "arm64"
            else -> arch
        }
    } else {
        arch
    }
}
val osInfo by lazy { "$osNameWithVersion $osArch" }
val isWindows by lazy { osName.toLowerCase(androidx.compose.ui.text.intl.Locale.current).contains("windows") }
val isMacOS by lazy { osName.toLowerCase(androidx.compose.ui.text.intl.Locale.current).contains("mac") }
val isMacOSWithArm: Boolean by lazy {
    try {
        val process = ProcessBuilder("sysctl", "-n", "hw.optional.arm64").start()
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        reader.readLine() == "1"
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}
val isRunningOnRosetta by lazy {
    val jvmIsArm = osRawArch == "aarch64"
    isMacOSWithArm && !jvmIsArm
}
val isLinux by lazy { osName.toLowerCase(androidx.compose.ui.text.intl.Locale.current).contains("linux") }
