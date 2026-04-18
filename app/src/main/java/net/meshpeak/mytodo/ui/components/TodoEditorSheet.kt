package net.meshpeak.mytodo.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.meshpeak.mytodo.R
import net.meshpeak.mytodo.domain.model.Priority
import net.meshpeak.mytodo.ui.common.label
import net.meshpeak.mytodo.ui.theme.MytodoTheme

data class TodoEditorInitial(
    val todoId: Long,
    val title: String,
    val note: String?,
    val priority: Priority,
)

data class TodoEditorResult(
    val title: String,
    val note: String?,
    val priority: Priority,
)

/**
 * TODO の新規作成 / 編集を行う ModalBottomSheet。
 * [initial] が null なら新規作成モード。フォルダは呼び出し側が保持しているものを使うため、
 * このシートではフォルダ選択 UI を持たない。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoEditorSheet(
    onDismiss: () -> Unit,
    onSubmit: (TodoEditorResult) -> Unit,
    initial: TodoEditorInitial? = null,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        TodoEditorForm(
            initial = initial,
            onCancel = onDismiss,
            onSubmit = { result ->
                onSubmit(result)
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) onDismiss()
                }
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun TodoEditorForm(
    initial: TodoEditorInitial?,
    onCancel: () -> Unit,
    onSubmit: (TodoEditorResult) -> Unit,
) {
    var title by rememberSaveable { mutableStateOf(initial?.title ?: "") }
    var note by rememberSaveable { mutableStateOf(initial?.note ?: "") }
    var priorityRank by rememberSaveable {
        mutableIntStateOf(initial?.priority?.rank ?: Priority.Unspecified.rank)
    }

    val selectedPriority = remember(priorityRank) { Priority.fromRank(priorityRank) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Text(
            text = stringResource(
                if (initial == null) R.string.sheet_title_new_todo else R.string.sheet_title_edit_todo,
            ),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text(stringResource(R.string.label_title)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text(stringResource(R.string.label_note)) },
            minLines = 2,
            maxLines = 4,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.label_priority),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Priority.entries.forEach { p ->
                FilterChip(
                    selected = p.rank == priorityRank,
                    onClick = { priorityRank = p.rank },
                    label = { Text(p.label()) },
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = onCancel) {
                Text(stringResource(R.string.action_cancel))
            }
            Spacer(Modifier.width(8.dp))
            Button(
                enabled = title.isNotBlank(),
                onClick = {
                    onSubmit(
                        TodoEditorResult(
                            title = title.trim(),
                            note = note.takeIf { it.isNotBlank() }?.trim(),
                            priority = selectedPriority,
                        ),
                    )
                },
            ) {
                Text(stringResource(R.string.action_save))
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun TodoEditorFormNewPreview() {
    MytodoTheme(dynamicColor = false) {
        Surface {
            TodoEditorForm(
                initial = null,
                onCancel = {},
                onSubmit = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TodoEditorFormEditPreview() {
    MytodoTheme(dynamicColor = false) {
        Surface {
            TodoEditorForm(
                initial = TodoEditorInitial(
                    todoId = 10,
                    title = "議事録をまとめる",
                    note = "次回会議は 1/20",
                    priority = Priority.Today,
                ),
                onCancel = {},
                onSubmit = {},
            )
        }
    }
}
