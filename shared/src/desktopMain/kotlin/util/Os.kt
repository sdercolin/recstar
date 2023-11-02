package util

val osName by lazy { System.getProperty("os.name").toString() }
val isWindows by lazy { osName.lowercase().contains("windows") }
val isMacOS by lazy { osName.lowercase().contains("mac") }
