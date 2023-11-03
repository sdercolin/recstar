package model

import androidx.compose.runtime.Immutable
import cafe.adriel.voyager.core.lifecycle.JavaSerializable
import io.File
import kotlinx.serialization.Serializable

/**
 * Model for a working session.
 *
 * @property name The name of the session. Must be a valid file name.
 * @property reclist The reclist to use for this session.
 * @property locationPath The absolute path to the directory where the recorded files will be saved.
 */
@Immutable
@Serializable
data class Session(
    val name: String,
    val reclist: Reclist,
    val locationPath: String,
) : JavaSerializable {
    val directory: File
        get() = File(locationPath)
}
