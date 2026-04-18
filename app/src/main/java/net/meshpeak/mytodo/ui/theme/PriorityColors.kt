package net.meshpeak.mytodo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import net.meshpeak.mytodo.domain.model.Priority

@Composable
@ReadOnlyComposable
fun Priority.tint(): Color = when (this) {
    Priority.Asap -> MaterialTheme.colorScheme.error
    Priority.Today -> MaterialTheme.colorScheme.tertiary
    Priority.Tomorrow -> MaterialTheme.colorScheme.secondary
    Priority.ThisWeek -> MaterialTheme.colorScheme.primary
    Priority.Someday -> MaterialTheme.colorScheme.outline
}
