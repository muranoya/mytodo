package net.meshpeak.mytodo.domain.usecase

import java.time.Clock
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import net.meshpeak.mytodo.domain.repository.TodoRepository

class PurgeExpiredTodosUseCase @Inject constructor(
    private val repo: TodoRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(): Int {
        val threshold = clock.instant().minus(TRASH_TTL_DAYS, ChronoUnit.DAYS)
        return repo.purgeOlderThan(threshold)
    }

    companion object {
        const val TRASH_TTL_DAYS: Long = 30
    }
}
