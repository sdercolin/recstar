package model

import io.File
import io.FileInteractor
import repository.ReclistRepository
import ui.common.AlertDialogController
import ui.common.ToastController
import ui.common.requestInput
import ui.common.show
import ui.screen.SessionScreenModel
import ui.string.*

/**
 * Defines an action that can be performed by the user. Typically by a global menu or a shortcut on Desktop.
 */
enum class Action {
    NewSession,
    ImportReclist,
    OpenDirectory,
    Exit,
    RenameSession,
    EditList,
    NextSentence,
    PreviousSentence,
    ToggleRecording,
    OpenAppDirectory,
    OpenContentDirectory,
    OpenAbout,
}

/**
 * A collection of action processing functions.
 */
object Actions {
    fun importReclist(
        fileInteractor: FileInteractor,
        repository: ReclistRepository,
        toastController: ToastController,
    ) {
        fileInteractor.pickFile(
            title = stringStatic(Strings.CreateSessionReclistScreenActionImport),
            allowedExtensions = listOf("txt"),
            onFinish = { file ->
                file ?: return@pickFile
                val imported = repository.import(file)
                if (imported) {
                    toastController.show(stringStatic(Strings.ToastImportReclistSuccess))
                } else {
                    toastController.show(stringStatic(Strings.ToastImportReclistFailure))
                }
            },
        )
    }

    fun openDirectory(
        fileInteractor: FileInteractor,
        directory: File,
    ) {
        fileInteractor.requestOpenFolder(directory)
    }

    fun renameSession(
        alertDialogController: AlertDialogController,
        model: SessionScreenModel,
    ) {
        alertDialogController.requestInput(
            title = stringStatic(Strings.SessionScreenActionRenameSession),
            initialValue = model.name,
            selected = true,
            onConfirmInput = model::renameSession,
        )
    }
}
