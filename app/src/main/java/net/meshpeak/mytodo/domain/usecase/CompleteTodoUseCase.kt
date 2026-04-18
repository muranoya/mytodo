package net.meshpeak.mytodo.domain.usecase

import java.time.Clock
import javax.inject.Inject
import net.meshpeak.mytodo.domain.repository.TodoRepository

class CompleteTodoUseCase @Inject constructor(
    private val repo: TodoRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(id: Long, completed: Boolean) {
        repo.setCompleted(
            id = id,
            isCompleted = completed,
            completedAt = if (completed) clock.instant() else null,
        )
    }
}
