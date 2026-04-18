package net.meshpeak.mytodo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import net.meshpeak.mytodo.domain.model.Priority

@Composable
@ReadOnlyComposable
fun Priority.tint(): Color {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    return when (this) {
        Priority.Asap -> if (isDark) PriorityAsapDark else PriorityAsapLight
        Priority.Today -> if (isDark) PriorityTodayDark else PriorityTodayLight
        Priority.Tomorrow -> if (isDark) PriorityTomorrowDark else PriorityTomorrowLight
        Priority.ThisWeek -> if (isDark) PriorityThisWeekDark else PriorityThisWeekLight
        Priority.Someday -> if (isDark) PrioritySomedayDark else PrioritySomedayLight
        Priority.Unspecified -> if (isDark) PriorityUnspecifiedDark else PriorityUnspecifiedLight
    }
}
