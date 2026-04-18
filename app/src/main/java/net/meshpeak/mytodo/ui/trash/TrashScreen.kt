package net.meshpeak.mytodo.ui.trash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.meshpeak.mytodo.R
import net.meshpeak.mytodo.ui.common.SnackbarEffect
import net.meshpeak.mytodo.ui.components.EmptyState
import net.meshpeak.mytodo.ui.components.SwipeToCompleteDelete
import net.meshpeak.mytodo.ui.components.TodoRow
import net.meshpeak.mytodo.ui.theme.MytodoTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    viewModel: TrashViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    SnackbarEffect(viewModel.events, snackbar)

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.nav_trash)) },
                actions = {
                    if (state.rows.isNotEmpty()) {
                        IconButton(onClick = { viewModel.emptyTrash() }) {
                            Icon(
                                Icons.Filled.DeleteForever,
                                contentDescription = stringResource(R.string.action_empty_trash),
                            )
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { inner ->
        Box(Modifier.fillMaxSize().padding(inner)) {
            when {
                state.isLoading -> Unit
                state.rows.isEmpty() -> EmptyState(
                    message = stringResource(R.string.empty_trash_title),
                    support = stringResource(R.string.empty_trash_support),
                )
                else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(items = state.rows, key = { "tr-${it.todo.id}" }) { row ->
                        SwipeToCompleteDelete(
                            onComplete = { viewModel.restore(row.todo.id) },
                            onDelete = { viewModel.purgeNow(row.todo.id) },
                            completeIcon = Icons.Filled.Restore,
                            deleteIcon = Icons.Filled.DeleteForever,
                        ) {
                            TodoRow(
                                todo = row.todo,
                                folderName = row.folderName,
                                onClick = {},
                            )
                        }
                    }
                }
            }
        }
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
