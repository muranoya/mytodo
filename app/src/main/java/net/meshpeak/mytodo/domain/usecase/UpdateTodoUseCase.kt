package net.meshpeak.mytodo.domain.usecase

import javax.inject.Inject
import net.meshpeak.mytodo.domain.model.Priority
import net.meshpeak.mytodo.domain.model.Todo
import net.meshpeak.mytodo.domain.repository.TodoRepository

class UpdateTodoUseCase @Inject constructor(
    private val repo: TodoRepository,
) {
    suspend operator fun invoke(
        current: Todo,
        folderId: Long,
        title: String,
        note: String?,
        priority: Priority,
    ): Long {
        val folderChanged = current.folderId != folderId
        val nextOrderIndex = if (folderChanged) Todo.UNASSIGNED_ORDER else current.orderIndex
        return repo.upsert(
            current.copy(
                folderId = folderId,
                title = title.trim(),
                note = note?.takeIf { it.isNotBlank() }?.trim(),
                priority = priority,
                orderIndex = nextOrderIndex,
            ),
        )
    }
}
