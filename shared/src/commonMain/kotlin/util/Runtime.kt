package util

import com.sdercolin.recstar.BuildKonfig

val isDebug = BuildKonfig.isDebug
val appVersion = BuildKonfig.version

expect fun quitApp()
