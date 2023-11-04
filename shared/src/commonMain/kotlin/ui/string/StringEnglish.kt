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
        AlertNeedManualPermissionGrantTitle -> "Permission Required"
        AlertNeedManualPermissionGrantMessage ->
            "The app needs your permission to record audio. Please grant the permission in system settings."
        ErrorReadFileFailedMessage -> "Failed to read file."
        ErrorExportDataFailedMessage -> "Failed to export data."
        ToastExportDataSuccess -> "Exported successfully"
        ToastExportDataCancel -> "Export cancelled"
        MainScreenAllSessions -> "All Sessions"
        MainScreenNewSession -> "Start New Session"
        MainScreenEmpty -> "No sessions yet."
        SessionScreenCurrentSentenceLabel -> "Current Recording: "
        SessionScreenActionOpenDirectory -> "Open Directory"
        SessionScreenActionExport -> "Export"
        CreateSessionReclistScreenTitle -> "Select Reclist"
        CreateSessionReclistScreenActionImport -> "Import Reclist"
        CreateSessionReclistScreenEmpty -> "Please import a reclist first."
        CreateSessionReclistScreenContinue -> "Finish"
        CreateSessionReclistScreenFailure -> "Failed to create session."
    }
