package ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import model.ListItem
import model.SortingMethod
import ui.model.LocalScreenOrientation
import ui.model.ScreenOrientation
import ui.string.*
import util.alpha

abstract class ItemListScreenModel<T : ListItem<T>>(
    private val alertDialogController: AlertDialogController,
    val allowedSortingMethods: List<SortingMethod>,
    initialSortingMethod: SortingMethod,
    val saveSortingMethod: (SortingMethod) -> Unit,
) : ScreenModel {
    abstract fun getDeleteAlertTitle(): String

    abstract fun getDeleteAlertMessage(count: Int): String

    @Composable
    abstract fun getItemsEmptyPlaceholder(): String

    abstract fun fetch()

    abstract val upstream: Flow<List<T>>

    abstract fun onClick(item: T)

    abstract fun onDelete(items: List<T>)

    open val isItemSelectable: Boolean = false

    open fun isItemSelected(name: String): Boolean = false

    var totalCount: Int = 0
        private set

    private val _items: MutableStateFlow<List<T>> = MutableStateFlow(emptyList())
    val items: StateFlow<List<T>> = _items

    private val searchTextState = mutableStateOf("")
    var searchText: String
        get() = searchTextState.value
        set(value) {
            searchTextState.value = value
            updateItems()
        }

    private val sortingMethodState = mutableStateOf(initialSortingMethod)
    var sortingMethod: SortingMethod
        get() = sortingMethodState.value
        set(value) {
            sortingMethodState.value = value
            saveSortingMethod(value)
            updateItems()
        }

    fun load() {
        fetch()
        screenModelScope.launch {
            upstream.collect { items ->
                _items.value = items.mapItems()
                totalCount = items.size
            }
        }
    }

    private fun List<T>.mapItems(): List<T> =
        filter { item -> searchText.isEmpty() || item.name.contains(searchText) }
            .let { sortingMethod.sort(it) }

    private fun updateItems() {
        screenModelScope.launch {
            _items.value = upstream.first().mapItems()
        }
    }

    var isEditing: Boolean by mutableStateOf(false)
        private set
    val selectedItemsForEdition: SnapshotStateList<T> = mutableStateListOf()

    fun startEditing() {
        isEditing = true
    }

    fun cancelEditing() {
        isEditing = false
        selectedItemsForEdition.clear()
    }

    fun isSelectedEdition(item: T): Boolean = item in selectedItemsForEdition

    private fun selectForEdition(
        item: T,
        isSelected: Boolean,
    ) {
        if (isSelected) {
            selectedItemsForEdition.add(item)
        } else {
            selectedItemsForEdition.remove(item)
        }
    }

    fun toggleSelectForEdition(item: T) {
        selectForEdition(item, !isSelectedEdition(item))
    }

    fun deleteSelectedItems() {
        if (selectedItemsForEdition.isEmpty()) {
            return
        }
        alertDialogController.requestConfirmCancellable(
            title = getDeleteAlertTitle(),
            message = getDeleteAlertMessage(selectedItemsForEdition.size),
            onConfirm = {
                val selected = selectedItemsForEdition.toList()
                cancelEditing()
                onDelete(selected)
            },
        )
    }
}

@Composable
fun <T : ListItem<T>> ItemListScreenContent(
    model: ItemListScreenModel<T>,
    title: String,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TitleBar(model, title)
        ItemDivider()
        ItemList(model)
    }
}

@Composable
private fun <T : ListItem<T>> TitleBar(
    model: ItemListScreenModel<T>,
    title: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = getWrappedTitleText(model, title),
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 24.dp),
            style = MaterialTheme.typography.h5,
        )
        val orientation = LocalScreenOrientation.current
        ReversedRow(
            modifier = Modifier.padding(start = if (orientation == ScreenOrientation.Portrait) 8.dp else 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SortingButton(
                initialMethod = model.sortingMethod,
                onMethodChanged = { model.sortingMethod = it },
                allowedMethods = model.allowedSortingMethods,
            )
            SearchBar(
                text = model.searchText,
                onTextChanged = { model.searchText = it },
            )
        }
    }
}

@Composable
private fun <T : ListItem<T>> ColumnScope.ItemList(model: ItemListScreenModel<T>) {
    Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
        val items by model.items.collectAsState()
        ScrollableLazyColumn {
            items(items, key = { it.name }) { item ->
                ItemRow(model, item, model::onClick) { isSelectingForDeletion ->
                    Text(item.name)
                    if (isSelectingForDeletion.not() && model.isItemSelectable && model.isItemSelected(item.name)) {
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            string(Strings.SelectedLabel),
                            style = MaterialTheme.typography.caption,
                        )
                    }
                }
                ItemDivider()
            }
        }
        if (items.isEmpty()) {
            Text(
                text = if (model.totalCount > 0) {
                    string(Strings.CommonNoMatch)
                } else {
                    model.getItemsEmptyPlaceholder()
                },
                modifier = Modifier.align(Alignment.Center).padding(16.dp),
            )
        }
    }
}

@Composable
fun <T : ListItem<T>> ActionButtonWrapper(
    model: ItemListScreenModel<T>,
    content: @Composable () -> Unit,
) {
    if (model.isEditing) {
        TextButton(
            onClick = { model.cancelEditing() },
        ) {
            Text(text = string(Strings.CommonCancel))
        }
    } else {
        content()
    }
}

@Composable
private fun <T : ListItem<T>> getWrappedTitleText(
    model: ItemListScreenModel<T>,
    text: String,
): String =
    if (model.isEditing) {
        string(Strings.MainScreenItemSelecting, model.selectedItemsForEdition.size)
    } else {
        text
    }

@Composable
private fun ItemDivider() {
    Divider(modifier = Modifier.padding(start = 16.dp))
}

@Composable
private fun <T : ListItem<T>> ItemRow(
    model: ItemListScreenModel<T>,
    item: T,
    onClick: (T) -> Unit,
    content: @Composable RowScope.(isSelectingForDeletion: Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp)
            .run {
                if (model.isEditing) {
                    plainClickable { model.toggleSelectForEdition(item) }
                } else {
                    clickable { onClick(item) }
                }
            }
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        if (model.isEditing) {
            val tint = if (model.isSelectedEdition(item)) {
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
        content(model.isEditing)
    }
}

@Composable
fun <T : ListItem<T>> BoxScope.FloatingActionButton(
    model: ItemListScreenModel<T>,
    icon: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    if (model.isEditing) {
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
