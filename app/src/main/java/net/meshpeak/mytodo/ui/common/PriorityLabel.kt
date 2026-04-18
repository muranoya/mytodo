package net.meshpeak.mytodo.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import net.meshpeak.mytodo.domain.model.Priority

@Composable
@ReadOnlyComposable
fun Priority.label(): String = stringResource(labelRes)
