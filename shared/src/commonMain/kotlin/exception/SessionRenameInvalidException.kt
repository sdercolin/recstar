package exception

import ui.string.*

class SessionRenameInvalidException(name: String) :
    LocalizedException(Strings.ExceptionRenameSessionInvalid, args = listOf(name))
