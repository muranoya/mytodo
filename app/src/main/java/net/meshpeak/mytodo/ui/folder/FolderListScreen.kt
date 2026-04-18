package net.meshpeak.mytodo.ui.folder

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.meshpeak.mytodo.R
import net.meshpeak.mytodo.ui.common.SnackbarEffect
import net.meshpeak.mytodo.ui.components.EmptyState
import net.meshpeak.mytodo.ui.components.FolderNameSheet
import net.meshpeak.mytodo.ui.theme.MytodoTheme
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderListScreen(
    onOpenFolder: (folderId: Long) -> Unit,
    viewModel: FolderListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var showCreateSheet by remember { mutableStateOf(false) }

    SnackbarEffect(viewModel.events, snackbar)

    Scaffold(
        topBar = {
            LargeTopAppBar(title = { Text(stringResource(R.string.nav_folders)) })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateSheet = true }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.action_new_folder))
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { inner ->
        Box(Modifier.fillMaxSize().padding(inner)) {
            when {
                state.isLoading -> Unit
                state.rows.isEmpty() -> EmptyState(
                    message = stringResource(R.string.empty_folders_title),
                    support = stringResource(R.string.empty_folders_support),
                )
                else -> ReorderableFolderList(
                    rows = state.rows,
                    onOpen = onOpenFolder,
                    onDragStarted = viewModel::onDragStarted,
                    onDragMove = viewModel::onDragMove,
                    onDragCommit = viewModel::onDragCommit,
                )
            }
        }
    }

    if (showCreateSheet) {
        FolderNameSheet(
            titleRes = R.string.sheet_title_new_folder,
            initialName = "",
            onDismiss = { showCreateSheet = false },
            onSubmit = { name ->
                viewModel.create(name)
                showCreateSheet = false
            },
        )
    }
}

@Composable
private fun ReorderableFolderList(
    rows: List<FolderRowUi>,
    onOpen: (Long) -> Unit,
    onDragStarted: () -> Unit,
    onDragMove: (Int, Int) -> Unit,
    onDragCommit: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        onDragMove(from.index, to.index)
    }
    LazyColumn(
        state = lazyListState,
        modifier = Modifier.fillMaxSize(),
    ) {
        items(count = rows.size, key = { index -> "f-${rows[index].folder.id}" }) { index ->
            val row = rows[index]
            ReorderableItem(state = reorderableState, key = "f-${row.folder.id}") { _ ->
                FolderListRow(
                    name = row.folder.name,
                    activeCount = row.activeCount,
                    onClick = { onOpen(row.folder.id) },
                    dragHandleModifier = Modifier.longPressDraggableHandle(
                        onDragStarted = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onDragStarted()
                        },
                        onDragStopped = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onDragCommit()
                        },
                    ),
                )
            }
        }
    }
}

@Composable
internal fun FolderListRow(
    name: String,
    activeCount: Int,
    onClick: () -> Unit,
    dragHandleModifier: Modifier = Modifier,
) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = if (activeCount == 0) {
                        stringResource(R.string.folder_row_empty)
                    } else {
                        stringResource(R.string.folder_row_count, activeCount)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = "$activeCount",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Filled.DragHandle,
                contentDescription = stringResource(R.string.action_drag_handle),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = dragHandleModifier,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FolderListRowPreview() {
    MytodoTheme(dynamicColor = false) {
        FolderListRow(
            name = "仕事",
            activeCount = 3,
            onClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FolderListRowEmptyPreview() {
    MytodoTheme(dynamicColor = false) {
        FolderListRow(
            name = "プライベート",
            activeCount = 0,
            onClick = {},
        )
    }
}
