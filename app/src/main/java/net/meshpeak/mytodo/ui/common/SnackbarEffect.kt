package net.meshpeak.mytodo.ui.common

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalResources
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

/**
 * ViewModel が流す [UiEvent] を [SnackbarHostState] に取り付ける。
 * Undo アクション押下時は [UiEvent.ShowSnackbar.onAction] を実行する。
 */
@Composable
fun SnackbarEffect(
    events: Flow<UiEvent>,
    hostState: SnackbarHostState,
) {
    val resources = LocalResources.current
    LaunchedEffect(events, hostState) {
        events.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    val message = if (event.messageArg != null) {
                        resources.getString(event.messageRes, event.messageArg)
                    } else {
                        resources.getString(event.messageRes)
                    }
                    val actionLabel = event.actionLabelRes?.let(resources::getString)
                    val result = hostState.showSnackbar(
                        message = message,
                        actionLabel = actionLabel,
                        withDismissAction = actionLabel == null,
                        duration = SnackbarDuration.Short,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        event.onAction?.invoke()
                    }
                }
            }
        }
    }
}
