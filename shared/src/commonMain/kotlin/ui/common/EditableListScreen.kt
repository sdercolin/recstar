package ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ui.string.*
import util.alpha

class EditableListConfig<T : Any>(
    val deleteAlertTitle: () -> String,
    val deleteAlertMessage: (count: Int) -> String,
    val onDelete: (List<T>) -> Unit,
)

interface EditableListScreenModel<T : Any> {
    val isSelectingForDeletion: Boolean
    val selectedItems: SnapshotStateList<T>

    fun startSelectingForDeletion()

    fun cancelSelectingForDeletion()

    fun isSelectedForDeletion(item: T): Boolean

    fun selectForDeletion(
        item: T,
        isSelected: Boolean,
    )

    fun deleteSelectedItems()

    fun toggleSelectForDeletion(item: T) {
        selectForDeletion(item, !isSelectedForDeletion(item))
    }

    @Composable
    fun ActionButtonWrapper(content: @Composable () -> Unit) {
        if (isSelectingForDeletion) {
            TextButton(
                onClick = { cancelSelectingForDeletion() },
            ) {
                Text(text = string(Strings.CommonCancel))
            }
        } else {
            content()
        }
    }

    @Composable
    fun getWrappedTitleText(text: String): String =
        if (isSelectingForDeletion) {
            string(Strings.MainScreenItemSelecting, selectedItems.size)
        } else {
            text
        }

    @Composable
    fun ItemRow(
        item: T,
        onClick: (T) -> Unit,
        content: @Composable RowScope.(isSelectingForDeletion: Boolean) -> Unit,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp)
                .run {
                    if (isSelectingForDeletion) {
                        plainClickable { toggleSelectForDeletion(item) }
                    } else {
                        clickable { onClick(item) }
                    }
                }
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            if (isSelectingForDeletion) {
                val tint = if (isSelectedForDeletion(item)) {
                    MaterialTheme.colors.primary
                } else {
                    MaterialTheme.colors.onSurface.alpha(0.3f)
                }
                Icon(
                    modifier = Modifier.padding(end = 16.dp).size(20.dp),
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = string(Strings.CommonCheck),
                    tint = tint,
                )
            }
            content(isSelectingForDeletion)
        }
    }
}

@Composable
fun <T : Any> BoxScope.FloatingActionButton(
    model: EditableListScreenModel<T>,
    icon: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    if (model.isSelectingForDeletion) {
        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(32.dp),
            backgroundColor = MaterialTheme.colors.secondaryVariant,
            onClick = { model.deleteSelectedItems() },
        ) {
            Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = string(Strings.CommonOkay),
            )
        }
    } else if (icon != null && onClick != null) {
        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(32.dp),
            backgroundColor = MaterialTheme.colors.primary,
            onClick = onClick,
        ) {
            icon()
        }
    }
}

class EditableListScreenModelImpl<T : Any>(
    private val alertDialogController: AlertDialogController,
    private val config: EditableListConfig<T>,
) : EditableListScreenModel<T> {
    override var isSelectingForDeletion: Boolean by mutableStateOf(false)
        private set
    override val selectedItems: SnapshotStateList<T> = mutableStateListOf()

    override fun startSelectingForDeletion() {
        isSelectingForDeletion = true
    }

    override fun cancelSelectingForDeletion() {
        isSelectingForDeletion = false
        selectedItems.clear()
    }

    override fun isSelectedForDeletion(item: T): Boolean = item in selectedItems

    override fun selectForDeletion(
        item: T,
        isSelected: Boolean,
    ) {
        if (isSelected) {
            selectedItems.add(item)
        } else {
            selectedItems.remove(item)
        }
    }

    override fun deleteSelectedItems() {
        if (selectedItems.isEmpty()) {
            return
        }
        alertDialogController.requestConfirmCancellable(
            title = config.deleteAlertTitle(),
            message = config.deleteAlertMessage(selectedItems.size),
            onConfirm = {
                val selected = selectedItems.toList()
                cancelSelectingForDeletion()
                config.onDelete(selected)
            },
        )
    }
}
