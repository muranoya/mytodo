package net.meshpeak.mytodo.domain.usecase

import java.time.Clock
import javax.inject.Inject
import net.meshpeak.mytodo.domain.model.Folder
import net.meshpeak.mytodo.domain.repository.FolderRepository

class CreateFolderUseCase @Inject constructor(
    private val repo: FolderRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(name: String): Long = repo.upsert(
        Folder(
            name = name.trim(),
            createdAt = clock.instant(),
        ),
    )
}
