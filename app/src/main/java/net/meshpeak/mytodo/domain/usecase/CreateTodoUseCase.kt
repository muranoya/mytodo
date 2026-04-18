package net.meshpeak.mytodo.domain.usecase

import java.time.Clock
import javax.inject.Inject
import net.meshpeak.mytodo.domain.model.Priority
import net.meshpeak.mytodo.domain.model.Todo
import net.meshpeak.mytodo.domain.repository.TodoRepository

class CreateTodoUseCase @Inject constructor(
    private val repo: TodoRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(
        folderId: Long,
        title: String,
        note: String?,
        priority: Priority,
    ): Long = repo.upsert(
        Todo(
            folderId = folderId,
            title = title.trim(),
            note = note?.takeIf { it.isNotBlank() }?.trim(),
            priority = priority,
            createdAt = clock.instant(),
        ),
    )
}
