package model

import kotlinx.serialization.Serializable

/**
 * A model class to hold the parameters for a session for serialization.
 */
@Serializable
data class SessionParams(
    val reclistName: String,
    val guideAudioName: String? = null,
)

fun Session.toParams(): SessionParams = SessionParams(reclist.name, guideAudioConfig?.name)
