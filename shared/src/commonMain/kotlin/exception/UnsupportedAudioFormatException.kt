package exception

import audio.AudioFormat
import ui.string.*

class UnsupportedAudioFormatException(val format: AudioFormat) :
    LocalizedException(
        Strings.ExceptionUnsupportedAudioFormat,
        args = listOf(format),
    ),
    FatalException
