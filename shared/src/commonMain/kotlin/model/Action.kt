package model

import io.File
import io.FileInteractor
import io.Paths
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import repository.AppPreferenceRepository
import repository.GuideAudioRepository
import repository.ReclistRepository
import ui.common.AlertDialogController
import ui.common.ToastController
import ui.common.requestConfirmError
import ui.common.requestInput
import ui.common.requestYesNo
import ui.common.show
import ui.screen.SessionScreenModel
import ui.string.*
import util.Log
import util.isDesktop
import util.quitApp

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
    ClearAppData,
    OpenAppDirectory,
    OpenContentDirectory,
    OpenAbout,
}

/**
 * A collection of action processing functions.
 */
object Actions {
    fun importReclist(
        scope: CoroutineScope,
        fileInteractor: FileInteractor,
        repository: ReclistRepository,
        alertDialogController: AlertDialogController,
        toastController: ToastController,
    ) {
        suspend fun import(
            file: File,
            commentFile: File?,
        ) {
            withContext(Dispatchers.IO) { repository.import(file, commentFile) }
                .onSuccess {
                    toastController.show(stringStatic(Strings.ToastImportSuccess))
                }.onFailure {
                    Log.e(it)
                    alertDialogController.requestConfirmError(
                        message = it.message ?: stringStatic(Strings.ToastImportFailure),
                    )
                }
        }

        fileInteractor.pickFile(
            title = stringStatic(Strings.CreateSessionReclistScreenActionImport),
            allowedExtensions = listOf(Reclist.FILE_EXTENSION),
            onFinish = { file ->
                file ?: return@pickFile
                alertDialogController.requestYesNo(
                    message = stringStatic(Strings.CreateSessionReclistScreenActionImportCommentAlertMessage),
                    onConfirm = {
                        fileInteractor.pickFile(
                            title = stringStatic(Strings.CreateSessionReclistScreenActionImport),
                            allowedExtensions = listOf(Reclist.FILE_EXTENSION),
                            onFinish = inner@{ commentFile ->
                                // cancel the import if `Yes` is clicked but no comment file is selected
                                commentFile ?: return@inner
                                scope.launch {
                                    import(file, commentFile)
                                }
                            },
                        )
                    },
                    onDismiss = {
                        scope.launch {
                            // import only the reclist file without comments if `No` is clicked
                            import(file, null)
                        }
                    },
                )
            },
        )
    }

    fun importGuideAudio(
        scope: CoroutineScope,
        fileInteractor: FileInteractor,
        repository: GuideAudioRepository,
        alertDialogController: AlertDialogController,
        toastController: ToastController,
    ) {
        suspend fun import(
            audioFile: File,
            configFile: File?,
            findConfig: Boolean = false,
        ) {
            withContext(Dispatchers.IO) { repository.import(audioFile, configFile, findConfig) }
                .onSuccess {
                    toastController.show(stringStatic(Strings.ToastImportSuccess))
                }.onFailure {
                    Log.e(it)
                    alertDialogController.requestConfirmError(
                        message = it.message ?: stringStatic(Strings.ToastImportFailure),
                    )
                }
        }

        fileInteractor.pickFile(
            title = stringStatic(Strings.GuideAudioScreenActionImport),
            allowedExtensions = listOf(GuideAudioRepository.GUIDE_AUDIO_FILE_EXTENSION),
            onFinish = { audioFile ->
                audioFile ?: return@pickFile
                if (isDesktop) {
                    scope.launch {
                        // We can directly access the file system on Desktop,
                        // so we can get the config file without further user interaction.
                        import(audioFile, null, findConfig = true)
                    }
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
                                scope.launch {
                                    import(audioFile, configFile)
                                }
                            },
                        )
                    },
                    onDismiss = {
                        // import only the audio file if `No` is clicked
                        scope.launch {
                            import(audioFile, null)
                        }
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

    fun clearAppData(alertDialogController: AlertDialogController) {
        alertDialogController.requestYesNo(
            title = stringStatic(Strings.MenuSettingsClearAppData),
            message = stringStatic(Strings.MenuSettingsClearAppDataAlertMessage),
            onConfirm = {
                Paths.appRoot.delete()
                quitApp()
            },
        )
    }

    fun clearSettings(
        alertDialogController: AlertDialogController,
        appPreferenceRepository: AppPreferenceRepository,
    ) {
        alertDialogController.requestYesNo(
            title = stringStatic(Strings.MenuSettingsClearSettings),
            message = stringStatic(Strings.MenuSettingsClearSettingsAlertMessage),
            onConfirm = {
                appPreferenceRepository.update { AppPreference() }
            },
        )
    }
}
