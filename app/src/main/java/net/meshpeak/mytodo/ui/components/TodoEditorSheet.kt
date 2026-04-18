package net.meshpeak.mytodo.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.Instant
import kotlinx.coroutines.launch
import net.meshpeak.mytodo.R
import net.meshpeak.mytodo.domain.model.Folder
import net.meshpeak.mytodo.domain.model.Priority
import net.meshpeak.mytodo.ui.common.label
import net.meshpeak.mytodo.ui.theme.MytodoTheme

data class TodoEditorInitial(
    val todoId: Long,
    val title: String,
    val note: String?,
    val priority: Priority,
    val folderId: Long,
)

data class TodoEditorResult(
    val title: String,
    val note: String?,
    val priority: Priority,
    val folderId: Long,
)

/**
 * TODO の新規作成 / 編集を行う ModalBottomSheet。
 * [initial] が null なら新規作成モード。新規モードでは [defaultFolderId] を初期選択フォルダに使う。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoEditorSheet(
    folders: List<Folder>,
    onDismiss: () -> Unit,
    onSubmit: (TodoEditorResult) -> Unit,
    initial: TodoEditorInitial? = null,
    defaultFolderId: Long? = null,
    folderLocked: Boolean = false,
) {
    if (folders.isEmpty()) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        TodoEditorForm(
            folders = folders,
            initial = initial,
            defaultFolderId = defaultFolderId,
            folderLocked = folderLocked,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TodoEditorForm(
    folders: List<Folder>,
    initial: TodoEditorInitial?,
    defaultFolderId: Long?,
    folderLocked: Boolean,
    onCancel: () -> Unit,
    onSubmit: (TodoEditorResult) -> Unit,
) {
    var title by rememberSaveable { mutableStateOf(initial?.title ?: "") }
    var note by rememberSaveable { mutableStateOf(initial?.note ?: "") }
    var priorityRank by rememberSaveable {
        mutableIntStateOf(initial?.priority?.rank ?: Priority.Today.rank)
    }
    var folderId by rememberSaveable {
        mutableLongStateOf(
            initial?.folderId
                ?: defaultFolderId
                ?: folders.first().id,
        )
    }

    val selectedPriority = remember(priorityRank) { Priority.fromRank(priorityRank) }
    val selectedFolder = remember(folderId, folders) {
        folders.firstOrNull { it.id == folderId } ?: folders.first()
    }

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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Priority.entries.forEach { p ->
                FilterChip(
                    selected = p.rank == priorityRank,
                    onClick = { priorityRank = p.rank },
                    label = { Text(p.label()) },
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        FolderSelector(
            folders = folders,
            selected = selectedFolder,
            enabled = !folderLocked,
            onSelected = { folderId = it.id },
        )
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
                            folderId = folderId,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FolderSelector(
    folders: List<Folder>,
    selected: Folder,
    enabled: Boolean,
    onSelected: (Folder) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { if (enabled) expanded = !expanded },
    ) {
        OutlinedTextField(
            value = selected.name,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(stringResource(R.string.label_folder)) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && enabled)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled),
        )
        ExposedDropdownMenu(
            expanded = expanded && enabled,
            onDismissRequest = { expanded = false },
        ) {
            folders.forEach { folder ->
                DropdownMenuItem(
                    text = { Text(folder.name) },
                    onClick = {
                        onSelected(folder)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TodoEditorFormNewPreview() {
    MytodoTheme(dynamicColor = false) {
        Surface {
            TodoEditorForm(
                folders = listOf(
                    Folder(id = 1, name = "仕事", orderIndex = 0, createdAt = Instant.EPOCH),
                    Folder(id = 2, name = "プライベート", orderIndex = 1, createdAt = Instant.EPOCH),
                ),
                initial = null,
                defaultFolderId = 1,
                folderLocked = false,
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
                folders = listOf(
                    Folder(id = 1, name = "仕事", orderIndex = 0, createdAt = Instant.EPOCH),
                ),
                initial = TodoEditorInitial(
                    todoId = 10,
                    title = "議事録をまとめる",
                    note = "次回会議は 1/20",
                    priority = Priority.Today,
                    folderId = 1,
                ),
                defaultFolderId = null,
                folderLocked = true,
                onCancel = {},
                onSubmit = {},
            )
        }
    }
}
