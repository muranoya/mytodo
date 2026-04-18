package net.meshpeak.mytodo.ui.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.meshpeak.mytodo.R
import net.meshpeak.mytodo.domain.model.Folder
import net.meshpeak.mytodo.domain.model.Todo
import net.meshpeak.mytodo.domain.repository.FolderRepository
import net.meshpeak.mytodo.domain.repository.TodoRepository
import net.meshpeak.mytodo.domain.usecase.RestoreTodoUseCase
import net.meshpeak.mytodo.ui.common.UiEvent

data class TrashRow(
    val todo: Todo,
    val folderName: String,
)

data class TrashUiState(
    val rows: List<TrashRow> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class TrashViewModel @Inject constructor(
    private val todoRepo: TodoRepository,
    folderRepo: FolderRepository,
    private val restoreTodo: RestoreTodoUseCase,
) : ViewModel() {

    private val _events = MutableSharedFlow<UiEvent>(extraBufferCapacity = 8)
    val events = _events.asSharedFlow()

    val uiState: StateFlow<TrashUiState> = combine(
        todoRepo.observeTrashed(),
        folderRepo.observeAll(),
    ) { todos, folders ->
        val byId = folders.associateBy(Folder::id)
        val rows = todos.map { TrashRow(it, byId[it.folderId]?.name.orEmpty()) }
        TrashUiState(rows = rows, isLoading = false)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TrashUiState(),
    )

    fun restore(id: Long) {
        viewModelScope.launch {
            restoreTodo(id)
            _events.emit(UiEvent.ShowSnackbar(messageRes = R.string.snackbar_restored))
        }
    }

    fun purgeNow(id: Long) {
        viewModelScope.launch {
            todoRepo.deleteById(id)
            _events.emit(UiEvent.ShowSnackbar(messageRes = R.string.snackbar_purged))
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            val removed = todoRepo.purgeAllTrashed()
            if (removed > 0) {
                _events.emit(UiEvent.ShowSnackbar(messageRes = R.string.snackbar_trash_emptied))
            }
        }
    }
}
