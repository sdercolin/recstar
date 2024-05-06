package exception

import audio.model.AudioFormat
import ui.string.*

class UnsupportedAudioFormatException(val format: AudioFormat) :
    LocalizedException(
        Strings.ExceptionUnsupportedAudioFormat,
        args = listOf(format),
    ),
    FatalException
