package io

import platform.UniformTypeIdentifiers.UTTypeAudio
import platform.UniformTypeIdentifiers.UTTypeData
import platform.UniformTypeIdentifiers.UTTypeText

object Uti {
    fun mapExtensions(extensions: List<String>): List<Any> {
        return extensions.map { extension ->
            when (extension) {
                "txt" -> UTTypeText
                "wav" -> UTTypeAudio
                else -> UTTypeData
            }
        }
    }
}
