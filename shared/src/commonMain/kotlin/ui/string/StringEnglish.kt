package ui.string

import ui.string.Strings.*

fun Strings.en(): String =
    when (this) {
        CommonOkay -> "OK"
        CommonCancel -> "Cancel"
        CommonYes -> "Yes"
        CommonNo -> "No"
        CommonBack -> "Back"
        CommonMore -> "More"
        CommonError -> "Error"
        AlertNeedManualPermissionGrantTitle -> "Permission Required"
        AlertNeedManualPermissionGrantMessage ->
            "The app needs your permission to record audio. Please grant the permission in system settings."
        ErrorReadFileFailedMessage -> "Failed to read file."
        ErrorExportDataFailedMessage -> "Failed to export data."
        ExceptionRenameSessionExisting -> "Session named {0} already exists."
        ExceptionRenameSessionInvalid -> "Invalid session name: {0}"
        ExceptionRenameSessionUnexpected -> "Failed to rename session."
        ToastExportDataSuccess -> "Exported successfully"
        ToastExportDataCancel -> "Export cancelled"
        MainScreenAllSessions -> "All Sessions"
        MainScreenNewSession -> "Start New Session"
        MainScreenEmpty -> "No sessions yet."
        SessionScreenCurrentSentenceLabel -> "Current Recording: "
        SessionScreenActionOpenDirectory -> "Open Directory"
        SessionScreenActionExport -> "Export"
        SessionScreenActionRenameSession -> "Rename Session"
        CreateSessionReclistScreenTitle -> "Select Reclist"
        CreateSessionReclistScreenActionImport -> "Import Reclist"
        CreateSessionReclistScreenEmpty -> "Please import a reclist first."
        CreateSessionReclistScreenContinue -> "Finish"
        CreateSessionReclistScreenFailure -> "Failed to create session."
    }
