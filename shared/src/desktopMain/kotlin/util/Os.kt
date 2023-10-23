package util

val osName by lazy { System.getProperty("os.name").toString() }
val isMacOS by lazy { osName.lowercase().contains("mac") }
