package util

actual val Locale: String by lazy {
    java.util.Locale.getDefault().toLanguageTag()
}
