package net.meshpeak.mytodo.ui.folder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import net.meshpeak.mytodo.domain.repository.FolderRepository
import net.meshpeak.mytodo.domain.repository.TodoRepository
import net.meshpeak.mytodo.domain.usecase.CreateFolderUseCase
import net.meshpeak.mytodo.ui.common.UiEvent

data class FolderRowUi(
    val folder: Folder,
    val activeCount: Int,
)

data class FolderListUiState(
    val rows: List<FolderRowUi> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class FolderListViewModel @Inject constructor(
    private val folderRepo: FolderRepository,
    todoRepo: TodoRepository,
    private val createFolder: CreateFolderUseCase,
) : ViewModel() {

    private val _events = MutableSharedFlow<UiEvent>(extraBufferCapacity = 8)
    val events = _events.asSharedFlow()

    private val dbRowsFlow: StateFlow<List<FolderRowUi>?> =
        combine(
            folderRepo.observeAll(),
            todoRepo.observeActiveCountPerFolder(),
        ) { folders, counts ->
            folders.map { f -> FolderRowUi(f, counts[f.id] ?: 0) }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    private val pendingOrder = MutableStateFlow<List<FolderRowUi>?>(null)
    private var preDragSnapshot: List<FolderRowUi>? = null

    val uiState: StateFlow<FolderListUiState> =
        combine(dbRowsFlow, pendingOrder) { db, pending ->
            FolderListUiState(
                rows = pending ?: db.orEmpty(),
                isLoading = db == null && pending == null,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FolderListUiState(),
        )

    fun create(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            createFolder(trimmed)
            _events.emit(UiEvent.ShowSnackbar(messageRes = R.string.snackbar_folder_created, messageArg = trimmed))
        }
    }

    fun rename(folderId: Long, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            val current = folderRepo.findById(folderId) ?: return@launch
            folderRepo.upsert(current.copy(name = trimmed))
            _events.emit(UiEvent.ShowSnackbar(messageRes = R.string.snackbar_folder_renamed, messageArg = trimmed))
        }
    }

    fun delete(folderId: Long) {
        viewModelScope.launch {
            folderRepo.deleteById(folderId)
            _events.emit(UiEvent.ShowSnackbar(messageRes = R.string.snackbar_folder_deleted))
        }
    }

    fun onDragStarted() {
        val snapshot = dbRowsFlow.value ?: return
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
        val sameOrder = pending.map { it.folder.id } == prev.map { it.folder.id }
        if (sameOrder) {
            pendingOrder.value = null
            return
        }
        val prevUpdates = prev.mapIndexed { i, r -> r.folder.id to i }
        val nextUpdates = pending.mapIndexed { i, r -> r.folder.id to i }
        viewModelScope.launch {
            folderRepo.reorder(nextUpdates)
            pendingOrder.value = null
            _events.emit(
                UiEvent.ShowSnackbar(
                    messageRes = R.string.snackbar_reorder_applied,
                    actionLabelRes = R.string.action_undo,
                    onAction = { viewModelScope.launch { folderRepo.reorder(prevUpdates) } },
                ),
            )
        }
    }

    fun onDragCancel() {
        preDragSnapshot = null
        pendingOrder.value = null
    }
}
