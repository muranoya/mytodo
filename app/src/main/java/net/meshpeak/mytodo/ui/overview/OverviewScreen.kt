package net.meshpeak.mytodo.ui.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.meshpeak.mytodo.R
import net.meshpeak.mytodo.domain.model.Todo
import net.meshpeak.mytodo.ui.common.AppSnackbarHost
import net.meshpeak.mytodo.ui.common.SnackbarEffect
import net.meshpeak.mytodo.ui.common.label
import net.meshpeak.mytodo.ui.components.EmptyState
import net.meshpeak.mytodo.ui.components.SwipeToCompleteDelete
import net.meshpeak.mytodo.ui.components.TodoEditorInitial
import net.meshpeak.mytodo.ui.components.TodoEditorSheet
import net.meshpeak.mytodo.ui.components.TodoRow
import net.meshpeak.mytodo.ui.theme.MytodoTheme
import net.meshpeak.mytodo.ui.theme.tint

@Composable
fun OverviewScreen(
    viewModel: OverviewViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var editing by remember { mutableStateOf<Todo?>(null) }

    SnackbarEffect(viewModel.events, snackbar)

    Scaffold(
        snackbarHost = { AppSnackbarHost(snackbar) },
    ) { inner ->
        Box(Modifier.fillMaxSize().padding(inner)) {
            when {
                state.isLoading -> Unit
                state.sections.isEmpty() -> EmptyState(
                    message = stringResource(R.string.empty_overview_title),
                    support = stringResource(R.string.empty_overview_support),
                )
                else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                    state.sections.forEachIndexed { index, section ->
                        if (index > 0) {
                            item(key = "d-${section.priority.rank}") {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                )
                            }
                        }
                        item(key = "h-${section.priority.rank}") {
                            PrioritySectionHeader(section.priority.label(), section.priority.tint())
                        }
                        items(items = section.rows, key = { "t-${it.todo.id}" }) { row ->
                            SwipeToCompleteDelete(
                                onComplete = { viewModel.complete(row.todo.id) },
                                onDelete = { viewModel.softDelete(row.todo.id) },
                            ) {
                                TodoRow(
                                    todo = row.todo,
                                    subtitle = row.folderName.takeIf { it.isNotBlank() },
                                    onClick = { editing = row.todo },
                                )
                            }
                        }
                    }
                }
            }
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
private fun PrioritySectionHeader(label: String, accent: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = accent,
        )
    }
}

@Preview
@Composable
private fun OverviewEmptyPreview() {
    MytodoTheme(dynamicColor = false) {
        EmptyState(
            message = stringResource(R.string.empty_overview_title),
            support = stringResource(R.string.empty_overview_support),
        )
    }
}
