import androidx.compose.runtime.Composable
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.MenuScope
import model.Action
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
            ActionItem(
                Action.ToggleRecording,
                string(Strings.MenuActionToggleRecording),
                shortcut = getShortcut(Key.Enter),
            )
        }
    }
}

@Composable
private fun MenuScope.ActionItem(
    action: Action,
    title: String,
    shortcut: KeyShortcut?,
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
