package ui.string

import ui.string.Strings.*

fun Strings.en(): String =
    when (this) {
        CommonOkay -> "OK"
        CommonCancel -> "Cancel"
        CommonYes -> "Yes"
        CommonNo -> "No"
        CommonBack -> "Back"
        AlertNeedManualPermissionGrantTitle -> "Permission Required"
        AlertNeedManualPermissionGrantMessage ->
            "The app needs your permission to record audio. Please grant the permission in system settings."
        ErrorReadFileFailedMessage -> "Failed to read file."
        MainScreenAllSessions -> "All Sessions"
        MainScreenNewSession -> "Start New Session"
        MainScreenEmpty -> "No sessions yet."
        RecorderScreenCurrentSentenceLabel -> "Current Recording: "
        CreateSessionReclistScreenTitle -> "Select Reclist"
        CreateSessionReclistScreenActionImport -> "Import Reclist"
        CreateSessionReclistScreenEmpty -> "Please import a reclist first."
        CreateSessionReclistScreenContinue -> "Finish"
        CreateSessionReclistScreenFailure -> "Failed to create session."
    }
