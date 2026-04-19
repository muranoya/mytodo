package net.meshpeak.mytodo.ui.folder

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
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
import net.meshpeak.mytodo.ui.navigation.TopRoute

data class FolderDetailUiState(
    val folder: Folder? = null,
    val todos: List<Todo> = emptyList(),
    val isLoading: Boolean = true,
    val isMissing: Boolean = false,
)

@HiltViewModel
class FolderDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val folderRepo: FolderRepository,
    private val todoRepo: TodoRepository,
    private val completeTodo: CompleteTodoUseCase,
    private val softDeleteTodo: SoftDeleteTodoUseCase,
    private val restoreTodo: RestoreTodoUseCase,
    private val createTodo: CreateTodoUseCase,
    private val updateTodo: UpdateTodoUseCase,
) : ViewModel() {

    private val folderId: Long = savedStateHandle.toRoute<TopRoute.FolderDetail>().folderId

    private val _events = MutableSharedFlow<UiEvent>(extraBufferCapacity = 8)
    val events = _events.asSharedFlow()

    private val pendingOrder = MutableStateFlow<List<Todo>?>(null)
    private var preDragSnapshot: List<Todo>? = null

    private val dbTodosFlow: StateFlow<List<Todo>?> =
        todoRepo.observeByFolder(folderId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    val uiState: StateFlow<FolderDetailUiState> = combine(
        folderRepo.observe(folderId),
        dbTodosFlow,
        pendingOrder,
    ) { folder, dbTodos, pending ->
        val loading = folder == null && dbTodos == null
        FolderDetailUiState(
            folder = folder,
            todos = pending ?: dbTodos.orEmpty(),
            isLoading = loading,
            isMissing = !loading && folder == null,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FolderDetailUiState(),
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

    fun onDragStarted() {
        val snapshot = dbTodosFlow.value ?: return
        preDragSnapshot = snapshot
        pendingOrder.value = snapshot
    }

    fun onDragMove(fromIndex: Int, toIndex: Int) {
        val current = pendingOrder.value ?: return
        if (fromIndex !in current.indices || toIndex !in current.indices || fromIndex == toIndex) return
        val mutable = current.toMutableList()
        mutable.add(toIndex, mutable.removeAt(fromIndex))
        pendingOrder.value = mutable
    }

    fun onDragCommit() {
        val pending = pendingOrder.value ?: return
        val prev = preDragSnapshot ?: pending
        preDragSnapshot = null
        val sameOrder = pending.map(Todo::id) == prev.map(Todo::id)
        if (sameOrder) {
            pendingOrder.value = null
            return
        }
        val prevUpdates = prev.mapIndexed { i, t -> t.id to i }
        val nextUpdates = pending.mapIndexed { i, t -> t.id to i }
        viewModelScope.launch {
            todoRepo.reorder(nextUpdates)
            pendingOrder.value = null
            _events.emit(
                UiEvent.ShowSnackbar(
                    messageRes = R.string.snackbar_reorder_applied,
                    actionLabelRes = R.string.action_undo,
                    onAction = { viewModelScope.launch { todoRepo.reorder(prevUpdates) } },
                ),
            )
        }
    }

    fun onDragCancel() {
        preDragSnapshot = null
        pendingOrder.value = null
    }
}
