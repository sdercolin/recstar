package model

import androidx.compose.runtime.Immutable
import cafe.adriel.voyager.core.lifecycle.JavaSerializable
import io.File
import util.isValidFileName

@Immutable
data class Reclist(
    val lines: List<String>,
) : JavaSerializable

fun loadReclist(file: File): Reclist {
    val lines = file.readText().lines().filter { it.isValidFileName() }
    return Reclist(lines)
}
