package net.meshpeak.mytodo.ui.trash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.meshpeak.mytodo.R
import net.meshpeak.mytodo.ui.common.SnackbarEffect
import net.meshpeak.mytodo.ui.components.EmptyState
import net.meshpeak.mytodo.ui.components.SwipeToCompleteDelete
import net.meshpeak.mytodo.ui.components.TodoRow
import net.meshpeak.mytodo.ui.theme.MytodoTheme

@Composable
fun TrashScreen(
    viewModel: TrashViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var showConfirm by remember { mutableStateOf(false) }
    SnackbarEffect(viewModel.events, snackbar)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
    ) { inner ->
        Box(Modifier.fillMaxSize().padding(inner)) {
            when {
                state.isLoading -> Unit
                state.rows.isEmpty() -> EmptyState(
                    message = stringResource(R.string.empty_trash_title),
                    support = stringResource(R.string.empty_trash_support),
                )
                else -> Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(onClick = { showConfirm = true }) {
                            Text(stringResource(R.string.action_empty_archive))
                        }
                    }
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(items = state.rows, key = { "tr-${it.todo.id}" }) { row ->
                            val statusRes = if (row.todo.deletedAt != null) {
                                R.string.archive_status_deleted
                            } else {
                                R.string.archive_status_completed
                            }
                            val status = stringResource(statusRes)
                            val subtitle = if (row.folderName.isNotBlank()) {
                                "$status · ${row.folderName}"
                            } else {
                                status
                            }
                            SwipeToCompleteDelete(
                                onComplete = { viewModel.restore(row.todo.id) },
                                onDelete = { viewModel.purgeNow(row.todo.id) },
                                completeIcon = Icons.Filled.Restore,
                                deleteIcon = Icons.Filled.DeleteForever,
                            ) {
                                TodoRow(
                                    todo = row.todo,
                                    subtitle = subtitle,
                                    onClick = {},
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text(stringResource(R.string.dialog_empty_archive_title)) },
            text = { Text(stringResource(R.string.dialog_empty_archive_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.emptyTrash()
                    showConfirm = false
                }) {
                    Text(stringResource(R.string.action_confirm_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

@Preview
@Composable
private fun TrashScreenEmptyPreview() {
    MytodoTheme(dynamicColor = false) {
        EmptyState(
            message = stringResource(R.string.empty_trash_title),
            support = stringResource(R.string.empty_trash_support),
        )
    }
}
