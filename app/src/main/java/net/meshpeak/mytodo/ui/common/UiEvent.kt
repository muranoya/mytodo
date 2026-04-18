package net.meshpeak.mytodo.ui.common

import androidx.annotation.StringRes

sealed interface UiEvent {
    data class ShowSnackbar(
        @StringRes val messageRes: Int,
        val messageArg: String? = null,
        @StringRes val actionLabelRes: Int? = null,
        val onAction: (suspend () -> Unit)? = null,
    ) : UiEvent
}
