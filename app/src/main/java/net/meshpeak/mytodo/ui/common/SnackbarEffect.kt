package net.meshpeak.mytodo.ui.common

import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalResources
import kotlinx.coroutines.flow.Flow

/**
 * SnackbarHost の slot で取り出して onAction を直接 onClick に配線できるよう、
 * UiEvent.ShowSnackbar の onAction を Snackbar 表示用のメタデータとして持ち回る。
 * showSnackbar の戻り値（ActionPerformed）を待つ方式は LaunchedEffect の disposal で
 * 落ちることがあるため、action ボタンの onClick から直接呼ぶ。
 */
private data class UiEventVisuals(
    override val message: String,
    val actionText: String?,
    val onAction: (() -> Unit)?,
) : SnackbarVisuals {
    override val actionLabel: String? get() = actionText
    override val duration: SnackbarDuration = SnackbarDuration.Short
    override val withDismissAction: Boolean get() = actionText == null
}

/**
 * ViewModel が流す [UiEvent] を [SnackbarHostState] に取り付ける。
 * 表示には [AppSnackbarHost] を使うこと（標準 SnackbarHost だと action が無反応になる）。
 */
@Composable
fun SnackbarEffect(
    events: Flow<UiEvent>,
    hostState: SnackbarHostState,
) {
    val resources = LocalResources.current
    LaunchedEffect(events, hostState) {
        events.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    val message = if (event.messageArg != null) {
                        resources.getString(event.messageRes, event.messageArg)
                    } else {
                        resources.getString(event.messageRes)
                    }
                    val actionLabel = event.actionLabelRes?.let(resources::getString)
                    hostState.showSnackbar(
                        UiEventVisuals(
                            message = message,
                            actionText = actionLabel,
                            onAction = event.onAction,
                        ),
                    )
                }
            }
        }
    }
}

/**
 * Scaffold の `snackbarHost` slot にこれを置く。action ボタンの onClick で
 * UiEventVisuals.onAction を直接呼ぶことで、コルーチン disposal の影響を受けない。
 */
@Composable
fun AppSnackbarHost(hostState: SnackbarHostState) {
    SnackbarHost(hostState) { data ->
        val visuals = data.visuals as? UiEventVisuals
        val actionText = visuals?.actionText
        val dismissState = rememberSwipeToDismissBoxState(
            confirmValueChange = { value ->
                if (value == SwipeToDismissBoxValue.StartToEnd ||
                    value == SwipeToDismissBoxValue.EndToStart
                ) {
                    data.dismiss()
                    true
                } else {
                    false
                }
            },
        )
        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {},
            enableDismissFromStartToEnd = true,
            enableDismissFromEndToStart = true,
        ) {
            Snackbar(
                action = if (actionText != null) {
                    {
                        TextButton(onClick = {
                            visuals.onAction?.invoke()
                            data.dismiss()
                        }) {
                            Text(actionText)
                        }
                    }
                } else {
                    null
                },
                content = { Text(data.visuals.message) },
            )
        }
    }
}
