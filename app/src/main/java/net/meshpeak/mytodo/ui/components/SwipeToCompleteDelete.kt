package net.meshpeak.mytodo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp

/**
 * TODO 行を右スワイプで完了、左スワイプで削除するラッパ。
 * 閾値を超えた瞬間に 1 回だけ触覚フィードバックを発火する。
 * 両方向を無効化したい場合は `enableComplete` / `enableDelete` を false に。
 *
 * @param onComplete 右スワイプ完了時のコールバック。呼び出し側がデータ更新で行を除去する想定。
 * @param onDelete 左スワイプ削除時のコールバック。
 */
@Composable
fun SwipeToCompleteDelete(
    onComplete: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    enableComplete: Boolean = true,
    enableDelete: Boolean = true,
    completeIcon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Filled.Check,
    completeBackground: Color = MaterialTheme.colorScheme.tertiaryContainer,
    completeTint: Color = MaterialTheme.colorScheme.onTertiaryContainer,
    deleteIcon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Filled.Delete,
    deleteBackground: Color = MaterialTheme.colorScheme.errorContainer,
    deleteTint: Color = MaterialTheme.colorScheme.onErrorContainer,
    content: @Composable () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val latestOnComplete by rememberUpdatedState(onComplete)
    val latestOnDelete by rememberUpdatedState(onDelete)

    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> enableComplete
                SwipeToDismissBoxValue.EndToStart -> enableDelete
                SwipeToDismissBoxValue.Settled -> false
            }
        },
    )

    LaunchedEffect(state.currentValue) {
        when (state.currentValue) {
            SwipeToDismissBoxValue.StartToEnd -> {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                latestOnComplete()
                state.reset()
            }
            SwipeToDismissBoxValue.EndToStart -> {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                latestOnDelete()
                state.reset()
            }
            SwipeToDismissBoxValue.Settled -> Unit
        }
    }

    SwipeToDismissBox(
        state = state,
        modifier = modifier,
        enableDismissFromStartToEnd = enableComplete,
        enableDismissFromEndToStart = enableDelete,
        backgroundContent = {
            val target = state.targetValue
            val (bg, align, icon, tint) = remember(target, completeBackground, deleteBackground) {
                when (target) {
                    SwipeToDismissBoxValue.StartToEnd -> BackgroundSpec(completeBackground, Alignment.CenterStart, completeIcon, completeTint)
                    SwipeToDismissBoxValue.EndToStart -> BackgroundSpec(deleteBackground, Alignment.CenterEnd, deleteIcon, deleteTint)
                    SwipeToDismissBoxValue.Settled -> BackgroundSpec(Color.Transparent, Alignment.CenterStart, completeIcon, completeTint)
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bg)
                    .padding(horizontal = 24.dp),
            ) {
                Row(
                    modifier = Modifier
                        .align(align)
                        .fillMaxSize(),
                    horizontalArrangement = if (align == Alignment.CenterStart) Arrangement.Start else Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (target != SwipeToDismissBoxValue.Settled) {
                        Icon(icon, contentDescription = null, tint = tint)
                    }
                }
            }
        },
        content = { content() },
    )
}

private data class BackgroundSpec(
    val background: Color,
    val alignment: Alignment,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val tint: Color,
)
