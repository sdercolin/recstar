import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.MenuScope
import model.Action
import repository.LocalAppActionStore
import repository.LocalAppPreferenceRepository
import ui.string.*
import util.isMacOS

@Composable
fun FrameWindowScope.Menu() {
    MenuBar {
        Menu(string(Strings.MenuFile), mnemonic = 'F') {
            ActionItem(
                Action.NewSession,
                string(Strings.MenuFileNewSession),
                shortcut = getShortcut(Key.N, ctrl = true),
            )
            ActionItem(
                Action.ImportReclist,
                string(Strings.MenuFileImportReclist),
                shortcut = getShortcut(Key.I, ctrl = true),
            )
            ActionItem(
                Action.ImportGuideAudio,
                string(Strings.MenuFileImportGuideAudio),
            )
            ActionItem(
                Action.OpenDirectory,
                string(Strings.MenuFileOpenDirectory),
                shortcut = getShortcut(Key.O, ctrl = true),
            )
            ActionItem(
                Action.Exit,
                string(Strings.MenuFileBack),
                shortcut = getShortcut(Key.B, ctrl = true),
            )
        }
        Menu(string(Strings.MenuEdit), mnemonic = 'E') {
            ActionItem(
                Action.RenameSession,
                string(Strings.MenuEditRenameSession),
                shortcut = getShortcut(Key.R, ctrl = true),
            )
            ActionItem(
                Action.ConfigureGuideAudio,
                string(Strings.MenuEditConfigureGuideAudio),
            )
            ActionItem(
                Action.EditList,
                string(Strings.MenuEditEditList),
                shortcut = getShortcut(Key.E, ctrl = true),
            )
        }
        Menu(string(Strings.MenuAction), mnemonic = 'A') {
            ActionItem(
                Action.NextSentence,
                string(Strings.MenuActionNextSentence),
                shortcut = getShortcut(Key.DirectionRight),
            )
            ActionItem(
                Action.PreviousSentence,
                string(Strings.MenuActionPreviousSentence),
                shortcut = getShortcut(Key.DirectionLeft),
            )
            val appPreference by LocalAppPreferenceRepository.current.flow.collectAsState()
            ActionItem(
                Action.ToggleRecording,
                if (appPreference.recording.recordWhileHolding) {
                    string(Strings.MenuActionToggleRecordingHoldingMode)
                } else {
                    string(Strings.MenuActionToggleRecording)
                },
                shortcut = getShortcut(appPreference.recording.recordingShortKey.getKey()),
            )
        }
        Menu(string(Strings.MenuSettings), mnemonic = 'S') {
            ActionItem(
                Action.OpenSettings,
                string(Strings.MenuSettingsOpenSettings),
            )
            ActionItem(
                Action.ClearSettings,
                string(Strings.MenuSettingsClearSettings),
            )
            ActionItem(
                Action.ClearAppData,
                string(Strings.MenuSettingsClearAppData),
            )
        }
        Menu(string(Strings.MenuHelp), mnemonic = 'H') {
            ActionItem(
                Action.OpenContentDirectory,
                string(Strings.MenuHelpOpenContentDirectory),
            )
            ActionItem(
                Action.OpenAppDirectory,
                string(Strings.MenuHelpOpenAppDirectory),
            )
            ActionItem(
                Action.OpenAbout,
                string(Strings.MenuHelpAbout),
            )
        }
    }
}

@Composable
private fun MenuScope.ActionItem(
    action: Action,
    title: String,
    shortcut: KeyShortcut? = null,
) {
    val store = LocalAppActionStore.current
    Item(
        title,
        shortcut = shortcut,
        enabled = store.isEnabled(action),
        onClick = { store.dispatch(action) },
    )
}

private fun getShortcut(
    key: Key,
    ctrl: Boolean = false,
    meta: Boolean = false,
    alt: Boolean = false,
    shift: Boolean = false,
): KeyShortcut {
    val mappedCtrl = if (isMacOS) meta else ctrl
    val mappedMeta = if (isMacOS) ctrl else meta
    return KeyShortcut(key, ctrl = mappedCtrl, meta = mappedMeta, alt = alt, shift = shift)
}
