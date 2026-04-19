package net.meshpeak.mytodo.ui.folder

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.Instant
import net.meshpeak.mytodo.R
import net.meshpeak.mytodo.domain.model.Priority
import net.meshpeak.mytodo.domain.model.Todo
import net.meshpeak.mytodo.ui.common.AppSnackbarHost
import net.meshpeak.mytodo.ui.common.SnackbarEffect
import net.meshpeak.mytodo.ui.components.EmptyState
import net.meshpeak.mytodo.ui.components.SwipeToCompleteDelete
import net.meshpeak.mytodo.ui.components.TodoEditorInitial
import net.meshpeak.mytodo.ui.components.TodoEditorSheet
import net.meshpeak.mytodo.ui.components.TodoRow
import net.meshpeak.mytodo.ui.theme.MytodoTheme
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderDetailScreen(
    onBack: () -> Unit,
    viewModel: FolderDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var editing by remember { mutableStateOf<Todo?>(null) }
    var showNewSheet by remember { mutableStateOf(false) }

    SnackbarEffect(viewModel.events, snackbar)

    LaunchedEffect(state.isMissing) {
        if (state.isMissing && !state.isLoading) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.folder?.name.orEmpty()) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showNewSheet = true }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.action_new_todo))
            }
        },
        snackbarHost = { AppSnackbarHost(snackbar) },
    ) { inner ->
        Box(Modifier.fillMaxSize().padding(inner)) {
            FolderDetailContent(
                todos = state.todos,
                isLoading = state.isLoading,
                onComplete = viewModel::complete,
                onSoftDelete = viewModel::softDelete,
                onEdit = { editing = it },
                onDragStarted = viewModel::onDragStarted,
                onDragMove = viewModel::onDragMove,
                onDragCommit = viewModel::onDragCommit,
            )
        }
    }

    if (showNewSheet) {
        val folderId = state.folder?.id
        if (folderId != null) {
            TodoEditorSheet(
                onDismiss = { showNewSheet = false },
                onSubmit = { result ->
                    viewModel.saveEditor(
                        initialTodo = null,
                        folderId = folderId,
                        title = result.title,
                        note = result.note,
                        priority = result.priority,
                    )
                },
            )
        }
    }

    editing?.let { target ->
        TodoEditorSheet(
            onDismiss = { editing = null },
            onSubmit = { result ->
                viewModel.saveEditor(
                    initialTodo = target,
                    folderId = target.folderId,
                    title = result.title,
                    note = result.note,
                    priority = result.priority,
                )
                editing = null
            },
            initial = TodoEditorInitial(
                todoId = target.id,
                title = target.title,
                note = target.note,
                priority = target.priority,
            ),
        )
    }
}

@Composable
internal fun FolderDetailContent(
    todos: List<Todo>,
    isLoading: Boolean,
    onComplete: (Long) -> Unit,
    onSoftDelete: (Long) -> Unit,
    onEdit: (Todo) -> Unit,
    onDragStarted: () -> Unit,
    onDragMove: (Int, Int) -> Unit,
    onDragCommit: () -> Unit,
) {
    when {
        isLoading -> Unit
        todos.isEmpty() -> EmptyState(
            message = stringResource(R.string.empty_folder_title),
            support = stringResource(R.string.empty_folder_support),
        )
        else -> {
            val haptic = LocalHapticFeedback.current
            val lazyListState = rememberLazyListState()
            val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
                onDragMove(from.index, to.index)
            }
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize(),
            ) {
                items(count = todos.size, key = { index -> "t-${todos[index].id}" }) { index ->
                    val todo = todos[index]
                    ReorderableItem(state = reorderableState, key = "t-${todo.id}") { _ ->
                        val handleModifier = Modifier.longPressDraggableHandle(
                            onDragStarted = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onDragStarted()
                            },
                            onDragStopped = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onDragCommit()
                            },
                        )
                        SwipeToCompleteDelete(
                            onComplete = { onComplete(todo.id) },
                            onDelete = { onSoftDelete(todo.id) },
                        ) {
                            TodoRow(
                                todo = todo,
                                subtitle = null,
                                onClick = { onEdit(todo) },
                                trailing = {
                                    Icon(
                                        imageVector = Icons.Filled.DragHandle,
                                        contentDescription = stringResource(R.string.action_drag_handle),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(end = 4.dp).then(handleModifier),
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FolderDetailContentPreview() {
    MytodoTheme(dynamicColor = false) {
        FolderDetailContent(
            todos = listOf(
                Todo(id = 1, folderId = 1, title = "会議の議事録をまとめる", priority = Priority.Today, createdAt = Instant.EPOCH),
                Todo(id = 2, folderId = 1, title = "請求書を送る", priority = Priority.Asap, createdAt = Instant.EPOCH),
                Todo(id = 3, folderId = 1, title = "来週の資料準備", priority = Priority.ThisWeek, createdAt = Instant.EPOCH),
            ),
            isLoading = false,
            onComplete = {},
            onSoftDelete = {},
            onEdit = {},
            onDragStarted = {},
            onDragMove = { _, _ -> },
            onDragCommit = {},
        )
    }
}
