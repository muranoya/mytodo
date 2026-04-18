package net.meshpeak.mytodo.domain.usecase

import javax.inject.Inject
import net.meshpeak.mytodo.domain.repository.TodoRepository

class RestoreTodoUseCase @Inject constructor(
    private val repo: TodoRepository,
) {
    suspend operator fun invoke(id: Long) {
        repo.restore(id)
    }
}
