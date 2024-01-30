package ui.string

import androidx.compose.runtime.Composable

enum class Strings {
    CommonOkay,
    CommonCancel,
    CommonYes,
    CommonNo,
    CommonBack,
    CommonMore,
    CommonError,
    CommonEdit,
    CommonCheck,
    CommonImport,
    CommonDoNotShowAgain,
    AlertNeedManualPermissionGrantTitle,
    AlertNeedManualPermissionGrantMessage,
    ErrorReadFileFailedMessage,
    ErrorExportDataFailedMessage,
    ExceptionRenameSessionExisting,
    ExceptionRenameSessionInvalid,
    ExceptionRenameSessionUnexpected,
    ExceptionUnsupportedAudioFormat,
    AlertExportTips,
    ToastExportDataSuccess,
    ToastExportDataCancel,
    ToastImportReclistSuccess,
    ToastImportReclistFailure,
    AlertUnexpectedErrorOpenLog,
    AlertUnexpectedErrorOpenLogButton,
    AlertUnexpectedErrorCopyLogToClipboard,
    AlertUnexpectedErrorCopyLogToClipboardButton,
    MainScreenAllSessions,
    MainScreenItemSelecting,
    MainScreenNewSession,
    MainScreenEmpty,
    MainScreenDeleteItemsTitle,
    MainScreenDeleteItemsMessage,
    SessionScreenCurrentSentenceLabel,
    SessionScreenCommentEmpty,
    SessionScreenNoData,
    SessionScreenActionOpenDirectory,
    SessionScreenActionExport,
    SessionScreenActionRenameSession,
    SessionScreenActionConfigureGuideAudio,
    SessionScreenActionSkipFinishedSentence,
    SessionScreenTogglePlaying,
    SessionScreenNoGuideAudio,
    SessionScreenAlertGuideAudioNotFoundMessage,
    CreateSessionReclistScreenTitle,
    CreateSessionReclistScreenActionImport,
    CreateSessionReclistScreenActionImportCommentAlertMessage,
    CreateSessionReclistScreenAllReclists,
    CreateSessionReclistScreenEmpty,
    CreateSessionReclistScreenContinue,
    CreateSessionReclistScreenFailure,
    CreateSessionReclistScreenDeleteItemsTitle,
    CreateSessionReclistScreenDeleteItemsMessage,
    GuideAudioScreenTitle,
    GuideAudioScreenActionImport,
    GuideAudioScreenActionImportConfigAlertMessage,
    GuideAudioScreenAllGuideAudios,
    GuideAudioScreenEmpty,
    GuideAudioScreenItemSelectedLabel,
    GuideAudioScreenDeleteItemsTitle,
    GuideAudioScreenDeleteItemsMessage,
    SortingMethod,
    SortingMethodNameAsc,
    SortingMethodNameDesc,
    SortingMethodUsedAsc,
    SortingMethodUsedDesc,
    PreferenceScreenTitle,
    PreferenceGroupAppearance,
    PreferenceLanguage,
    PreferenceLanguageAuto,
    PreferenceTheme,
    PreferenceThemeSystem,
    PreferenceThemeLight,
    PreferenceThemeDark,
    PreferenceOrientation,
    PreferenceOrientationAuto,
    PreferenceOrientationPortrait,
    PreferenceOrientationLandscape,
    PreferenceGroupRecording,
    PreferenceContinuousRecording,
    PreferenceContinuousRecordingDescription,
    PreferenceTrimRecording,
    PreferenceTrimRecordingDescription,
    PreferenceRecordWhileHolding,
    PreferenceRecordingShortKey,
    PreferenceRecordingShortKeyEnter,
    PreferenceRecordingShortKeyR,
    PreferenceAutoListenBack,
    PreferenceAutoListenBackDescription,
    PreferenceAutoNext,
    PreferenceAutoNextDescription,
    PreferenceGroupAudio,
    PreferenceInputDeviceName,
    PreferenceOutputDeviceName,
    PreferenceSampleRate,
    PreferenceBitDepth,
    PreferenceDeviceNameNotFoundTemplate,
    PreferenceGroupView,
    PreferenceTitleBarStyle,
    PreferenceTitleBarStyleFileName,
    PreferenceTitleBarStyleFileNameWithComment,
    PreferenceTitleBarStyleCommentWithFileName,
    PreferenceTitleBarStyleComment,
    PreferenceGroupMisc,
    PreferenceContentRootLocation,
    PreferenceAbout,
    AboutScreenPrivacyPolicy,
    AboutScreenCopyDeviceInfo,
    AboutScreenDeviceInfoCopied,
    AboutScreenViewLicenses,
    AboutScreenViewOnGithub,
    LicenseScreenTitle,
    MenuFile,
    MenuFileNewSession,
    MenuFileImportReclist,
    MenuFileImportGuideAudio,
    MenuFileOpenDirectory,
    MenuFileBack,
    MenuEdit,
    MenuEditRenameSession,
    MenuEditConfigureGuideAudio,
    MenuEditEditList,
    MenuAction,
    MenuActionNextSentence,
    MenuActionPreviousSentence,
    MenuActionToggleRecording,
    MenuActionToggleRecordingHoldingMode,
    MenuSettings,
    MenuSettingsOpenSettings,
    MenuSettingsClearSettings,
    MenuSettingsClearSettingsAlertMessage,
    MenuHelp,
    MenuHelpOpenContentDirectory,
    MenuHelpOpenAppDirectory,
    MenuHelpAbout,
    ;

    fun get(language: Language): String =
        when (language) {
            Language.English -> en()
            Language.ChineseSimplified -> zhHans()
            Language.Japanese -> ja()
        } ?: en()
}

/**
 * Get a string in the current language. Recomposition will be triggered when [LocalLanguage] changes.
 */
@Composable
fun string(
    key: Strings,
    vararg params: Any?,
): String = stringCertain(key, LocalLanguage.current, *params)

/**
 * Get a string in the current language. Note this is not a Composable function, so when [LocalLanguage] changes, the
 * string won't update.
 */
fun stringStatic(
    key: Strings,
    vararg params: Any?,
): String = stringCertain(key, currentLanguage, *params)

/**
 * Get a string in the given [language].
 */
fun stringCertain(
    key: Strings,
    language: Language,
    vararg params: Any?,
): String {
    val template = key.get(language)
    return stringFormat(template, *params)
}

/**
 * A simplified C#-style string format function.
 *
 * @sample stringFormat("Hello, {0}!", "world") // "Hello, world!"
 * @sample stringFormat("Hello, {0}! {0} {1}", "world", 123) // "Hello, world! world 123"
 * @sample stringFormat("Hello, {{0}}!") // "Hello, {0}!"
 */
private fun stringFormat(
    format: String,
    vararg args: Any?,
): String {
    val formattedString = StringBuilder()
    var i = 0

    while (i < format.length) {
        when (format[i]) {
            '{' -> {
                if (i + 1 < format.length && format[i + 1] == '{') {
                    // Escaped '{'
                    formattedString.append('{')
                    i += 2
                } else {
                    val indexEnd = format.indexOf('}', i)
                    if (indexEnd == -1) throw IllegalArgumentException("Unmatched '{'")

                    val placeholder = format.substring(i, indexEnd + 1)
                    val index = placeholder.substring(1, placeholder.length - 1).toIntOrNull()
                        ?: throw IllegalArgumentException("Invalid placeholder: $placeholder")

                    if (index >= args.size) throw IllegalArgumentException("Index out of bounds: $index")

                    formattedString.append(args[index].toString())
                    i = indexEnd + 1
                }
            }
            '}' -> {
                if (i + 1 < format.length && format[i + 1] == '}') {
                    // Escaped '}'
                    formattedString.append('}')
                    i += 2
                } else {
                    throw IllegalArgumentException("Unmatched '}'")
                }
            }
            else -> {
                formattedString.append(format[i])
                i++
            }
        }
    }
    return formattedString.toString()
}
