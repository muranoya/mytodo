package net.meshpeak.mytodo.domain.usecase

import java.time.Clock
import javax.inject.Inject
import net.meshpeak.mytodo.domain.repository.TodoRepository

class SoftDeleteTodoUseCase @Inject constructor(
    private val repo: TodoRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(id: Long) {
        repo.setDeletedAt(id = id, deletedAt = clock.instant())
    }
}
