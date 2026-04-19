package net.meshpeak.mytodo.ui.overview

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
import net.meshpeak.mytodo.domain.model.Priority
import net.meshpeak.mytodo.domain.model.Todo
import net.meshpeak.mytodo.domain.repository.FolderRepository
import net.meshpeak.mytodo.domain.repository.TodoRepository
import net.meshpeak.mytodo.domain.usecase.CompleteTodoUseCase
import net.meshpeak.mytodo.domain.usecase.CreateTodoUseCase
import net.meshpeak.mytodo.domain.usecase.RestoreTodoUseCase
import net.meshpeak.mytodo.domain.usecase.SoftDeleteTodoUseCase
import net.meshpeak.mytodo.domain.usecase.UpdateTodoUseCase
import net.meshpeak.mytodo.ui.common.UiEvent

data class OverviewRow(
    val todo: Todo,
    val folderName: String,
)

data class OverviewSection(
    val priority: Priority,
    val rows: List<OverviewRow>,
)

data class OverviewUiState(
    val sections: List<OverviewSection> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class OverviewViewModel @Inject constructor(
    private val todoRepo: TodoRepository,
    private val folderRepo: FolderRepository,
    private val completeTodo: CompleteTodoUseCase,
    private val softDeleteTodo: SoftDeleteTodoUseCase,
    private val restoreTodo: RestoreTodoUseCase,
    private val createTodo: CreateTodoUseCase,
    private val updateTodo: UpdateTodoUseCase,
) : ViewModel() {

    private val _events = MutableSharedFlow<UiEvent>(extraBufferCapacity = 8)
    val events = _events.asSharedFlow()

    val uiState: StateFlow<OverviewUiState> =
        combine(
            todoRepo.observeActive(),
            folderRepo.observeAll(),
        ) { todos, folders ->
            val folderById = folders.associateBy(Folder::id)
            val rows = todos.map { t ->
                OverviewRow(todo = t, folderName = folderById[t.folderId]?.name.orEmpty())
            }
            val sections = rows
                .groupBy { it.todo.priority }
                .toSortedMap(compareBy { it.rank })
                .map { (priority, rs) -> OverviewSection(priority, rs) }
            OverviewUiState(sections = sections, isLoading = false)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = OverviewUiState(),
        )

    fun complete(id: Long) {
        viewModelScope.launch {
            if (todoRepo.findById(id) == null) return@launch
            completeTodo(id, completed = true)
            _events.emit(
                UiEvent.ShowSnackbar(
                    messageRes = R.string.snackbar_completed,
                    actionLabelRes = R.string.action_undo,
                    onAction = {
                        viewModelScope.launch { restoreTodo(id) }
                    },
                ),
            )
        }
    }

    fun softDelete(id: Long) {
        viewModelScope.launch {
            if (todoRepo.findById(id) == null) return@launch
            softDeleteTodo(id)
            _events.emit(
                UiEvent.ShowSnackbar(
                    messageRes = R.string.snackbar_moved_to_trash,
                    actionLabelRes = R.string.action_undo,
                    onAction = {
                        viewModelScope.launch { restoreTodo(id) }
                    },
                ),
            )
        }
    }

    fun saveEditor(initialTodo: Todo?, folderId: Long, title: String, note: String?, priority: Priority) {
        viewModelScope.launch {
            if (initialTodo == null) {
                createTodo(folderId = folderId, title = title, note = note, priority = priority)
            } else {
                updateTodo(current = initialTodo, folderId = folderId, title = title, note = note, priority = priority)
            }
        }
    }
}
