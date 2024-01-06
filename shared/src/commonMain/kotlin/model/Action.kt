package model

import io.File
import io.FileInteractor
import repository.GuideAudioRepository
import repository.ReclistRepository
import ui.common.AlertDialogController
import ui.common.ToastController
import ui.common.requestInput
import ui.common.requestYesNo
import ui.common.show
import ui.screen.SessionScreenModel
import ui.string.*
import util.isDesktop

/**
 * Defines an action that can be performed by the user. Typically by a global menu or a shortcut on Desktop.
 */
enum class Action {
    NewSession,
    ImportReclist,
    ImportGuideAudio,
    OpenDirectory,
    Exit,
    RenameSession,
    ConfigureGuideAudio,
    EditList,
    NextSentence,
    PreviousSentence,
    ToggleRecording,
    OpenSettings,
    ClearSettings,
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
        alertDialogController: AlertDialogController,
        toastController: ToastController,
    ) {
        fun import(
            file: File,
            commentFile: File?,
            findComment: Boolean = false,
        ) {
            val imported = repository.import(file, commentFile, findComment)
            if (imported) {
                toastController.show(stringStatic(Strings.ToastImportReclistSuccess))
            } else {
                toastController.show(stringStatic(Strings.ToastImportReclistFailure))
            }
        }

        fileInteractor.pickFile(
            title = stringStatic(Strings.CreateSessionReclistScreenActionImport),
            allowedExtensions = listOf(Reclist.FILE_EXTENSION),
            onFinish = { file ->
                file ?: return@pickFile
                if (isDesktop) {
                    // We can directly access the file system on Desktop,
                    // so we can get the comment file without further user interaction.
                    import(file, null, findComment = true)
                    return@pickFile
                }
                alertDialogController.requestYesNo(
                    message = stringStatic(Strings.CreateSessionReclistScreenActionImportCommentAlertMessage),
                    onConfirm = {
                        fileInteractor.pickFile(
                            title = stringStatic(Strings.CreateSessionReclistScreenActionImport),
                            allowedExtensions = listOf(Reclist.FILE_EXTENSION),
                            onFinish = inner@{ commentFile ->
                                // cancel the import if `Yes` is clicked but no comment file is selected
                                commentFile ?: return@inner
                                import(file, commentFile)
                            },
                        )
                    },
                    onDismiss = {
                        // import only the reclist file without comments if `No` is clicked
                        import(file, null)
                    },
                )
            },
        )
    }

    fun importGuideAudio(
        fileInteractor: FileInteractor,
        repository: GuideAudioRepository,
        alertDialogController: AlertDialogController,
        toastController: ToastController,
    ) {
        fun import(
            audioFile: File,
            configFile: File?,
            findConfig: Boolean = false,
        ) {
            val imported = repository.import(audioFile, configFile, findConfig)
            if (imported) {
                toastController.show(stringStatic(Strings.ToastImportReclistSuccess))
            } else {
                toastController.show(stringStatic(Strings.ToastImportReclistFailure))
            }
        }

        fileInteractor.pickFile(
            title = stringStatic(Strings.GuideAudioScreenActionImport),
            allowedExtensions = listOf(GuideAudioRepository.GUIDE_AUDIO_FILE_EXTENSION),
            onFinish = { audioFile ->
                audioFile ?: return@pickFile
                if (isDesktop) {
                    // We can directly access the file system on Desktop,
                    // so we can get the config file without further user interaction.
                    import(audioFile, null, findConfig = true)
                    return@pickFile
                }
                alertDialogController.requestYesNo(
                    message = stringStatic(Strings.GuideAudioScreenActionImportConfigAlertMessage),
                    onConfirm = {
                        fileInteractor.pickFile(
                            title = stringStatic(Strings.GuideAudioScreenActionImport),
                            allowedExtensions = listOf(GuideAudioRepository.GUIDE_AUDIO_RAW_CONFIG_FILE_EXTENSION),
                            onFinish = inner@{ configFile ->
                                // cancel the import if `Yes` is clicked but no config file is selected
                                configFile ?: return@inner
                                import(audioFile, configFile)
                            },
                        )
                    },
                    onDismiss = {
                        // import only the audio file if `No` is clicked
                        import(audioFile, null)
                    },
                )
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
