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
        CommonEdit -> "Edit"
        CommonCheck -> "Check"
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
        MainScreenItemSelecting -> "Selecting {0} items"
        MainScreenNewSession -> "Start New Session"
        MainScreenEmpty -> "No sessions yet."
        MainScreenDeleteItemsConfirmationTitle -> "Delete Sessions"
        MainScreenDeleteItemsConfirmationMessage ->
            "Are you sure you want to delete {0} sessions? The recordings will be completely removed from the device."
        SessionScreenCurrentSentenceLabel -> "Current Recording: "
        SessionScreenNoData -> "No data."
        SessionScreenActionOpenDirectory -> "Open Directory"
        SessionScreenActionExport -> "Export"
        SessionScreenActionRenameSession -> "Rename Session"
        CreateSessionReclistScreenTitle -> "Select Reclist"
        CreateSessionReclistScreenActionImport -> "Import Reclist"
        CreateSessionReclistScreenEmpty -> "Please import a reclist first."
        CreateSessionReclistScreenContinue -> "Finish"
        CreateSessionReclistScreenFailure -> "Failed to create session."
    }
