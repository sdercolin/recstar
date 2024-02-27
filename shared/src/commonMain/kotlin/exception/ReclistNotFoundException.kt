package exception

import ui.string.*

class ReclistNotFoundException(reclistName: String) :
    LocalizedException(Strings.ExceptionReclistNotFound, args = listOf(reclistName))
