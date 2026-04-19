package net.meshpeak.mytodo.ui.components

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.meshpeak.mytodo.R

/**
 * TODO 行を右スワイプで完了、左スワイプで削除するラッパ。
 * ドラッグ開始直後から方向に応じた色とラベルを表示し、閾値到達で背景濃化＋アイコン拡大に切り替える。
 *
 * @param onComplete 右スワイプ確定時のコールバック。呼び出し側がデータ更新で行を除去する想定。
 * @param onDelete 左スワイプ確定時のコールバック。
 */
@Composable
fun SwipeToCompleteDelete(
    onComplete: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    enableComplete: Boolean = true,
    enableDelete: Boolean = true,
    completeIcon: ImageVector = Icons.Filled.Check,
    @StringRes completeLabelRes: Int = R.string.swipe_label_complete,
    deleteIcon: ImageVector = Icons.Filled.Delete,
    @StringRes deleteLabelRes: Int = R.string.swipe_label_delete,
    content: @Composable () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val latestOnComplete by rememberUpdatedState(onComplete)
    val latestOnDelete by rememberUpdatedState(onDelete)

    // 目標アンカーへの遷移を常に拒否することで state.currentValue を Settled に固定する。
    // LazyColumn のアイテム再構成時に rememberSaveable が StartToEnd/EndToStart を復元して
    // 再スワイプ扱いになるのを防ぐため。アクションは遷移確定前に confirmValueChange 内で発火。
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    if (enableComplete) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        latestOnComplete()
                    }
                    false
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    if (enableDelete) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        latestOnDelete()
                    }
                    false
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        },
    )

    val completeBgSoft = MaterialTheme.colorScheme.tertiaryContainer
    val completeFgSoft = MaterialTheme.colorScheme.onTertiaryContainer
    val completeBgStrong = MaterialTheme.colorScheme.tertiary
    val completeFgStrong = MaterialTheme.colorScheme.onTertiary
    val deleteBgSoft = MaterialTheme.colorScheme.errorContainer
    val deleteFgSoft = MaterialTheme.colorScheme.onErrorContainer
    val deleteBgStrong = MaterialTheme.colorScheme.error
    val deleteFgStrong = MaterialTheme.colorScheme.onError

    SwipeToDismissBox(
        state = state,
        modifier = modifier,
        enableDismissFromStartToEnd = enableComplete,
        enableDismissFromEndToStart = enableDelete,
        backgroundContent = {
            val offset = runCatching { state.requireOffset() }.getOrDefault(0f)
            val isCompleteSide = offset > 0.5f
            val isDeleteSide = offset < -0.5f
            val reached = state.targetValue != SwipeToDismissBoxValue.Settled

            val iconSize by animateDpAsState(
                targetValue = if (reached) 32.dp else 24.dp,
                label = "swipeIconSize",
            )

            val bg = when {
                isCompleteSide -> if (reached) completeBgStrong else completeBgSoft
                isDeleteSide -> if (reached) deleteBgStrong else deleteBgSoft
                else -> Color.Transparent
            }
            val fg = when {
                isCompleteSide -> if (reached) completeFgStrong else completeFgSoft
                isDeleteSide -> if (reached) deleteFgStrong else deleteFgSoft
                else -> Color.Transparent
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bg)
                    .padding(horizontal = 24.dp),
            ) {
                if (isCompleteSide) {
                    Row(
                        modifier = Modifier.align(Alignment.CenterStart),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            completeIcon,
                            contentDescription = null,
                            tint = fg,
                            modifier = Modifier.size(iconSize),
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = stringResource(completeLabelRes),
                            color = fg,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                } else if (isDeleteSide) {
                    Row(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(deleteLabelRes),
                            color = fg,
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Spacer(Modifier.width(12.dp))
                        Icon(
                            deleteIcon,
                            contentDescription = null,
                            tint = fg,
                            modifier = Modifier.size(iconSize),
                        )
                    }
                }
            }
        },
        content = { content() },
    )
}
