package model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class AppRecord(
    val windowSizeDp: Pair<Float, Float> = Pair(800f, 500f),
)
