package net.meshpeak.mytodo.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import net.meshpeak.mytodo.domain.usecase.PurgeExpiredTodosUseCase

@HiltWorker
class TrashCleanerWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val purgeExpiredTodos: PurgeExpiredTodosUseCase,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result =
        runCatching { purgeExpiredTodos() }
            .fold(
                onSuccess = { Result.success() },
                onFailure = { Result.retry() },
            )

    companion object {
        const val UNIQUE_NAME: String = "trash_cleaner"
    }
}
