package exception

import ui.string.*

class SessionRenameExistingException(name: String) :
    LocalizedException(Strings.ExceptionRenameSessionExisting, args = listOf(name))
