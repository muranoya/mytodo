package net.meshpeak.mytodo.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.meshpeak.mytodo.R
import net.meshpeak.mytodo.ui.theme.MytodoTheme

/**
 * フォルダ名の新規入力 / 改名用 ModalBottomSheet。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderNameSheet(
    @StringRes titleRes: Int,
    initialName: String,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        FolderNameForm(
            titleRes = titleRes,
            initialName = initialName,
            onCancel = onDismiss,
            onSubmit = { name ->
                onSubmit(name)
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) onDismiss()
                }
            },
        )
    }
}

@Composable
internal fun FolderNameForm(
    @StringRes titleRes: Int,
    initialName: String,
    onCancel: () -> Unit,
    onSubmit: (String) -> Unit,
) {
    var name by rememberSaveable(initialName) { mutableStateOf(initialName) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.label_folder_name)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
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
                enabled = name.isNotBlank(),
                onClick = { onSubmit(name.trim()) },
            ) {
                Text(stringResource(R.string.action_save))
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun FolderNameFormCreatePreview() {
    MytodoTheme(dynamicColor = false) {
        Surface {
            FolderNameForm(
                titleRes = R.string.sheet_title_new_folder,
                initialName = "",
                onCancel = {},
                onSubmit = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FolderNameFormRenamePreview() {
    MytodoTheme(dynamicColor = false) {
        Surface {
            FolderNameForm(
                titleRes = R.string.sheet_title_rename_folder,
                initialName = "仕事",
                onCancel = {},
                onSubmit = {},
            )
        }
    }
}
