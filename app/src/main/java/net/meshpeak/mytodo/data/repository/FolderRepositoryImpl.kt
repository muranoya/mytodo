package net.meshpeak.mytodo.data.repository

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.meshpeak.mytodo.data.local.FolderDao
import net.meshpeak.mytodo.data.local.FolderEntity
import net.meshpeak.mytodo.domain.model.Folder
import net.meshpeak.mytodo.domain.repository.FolderRepository

class FolderRepositoryImpl @Inject constructor(
    private val dao: FolderDao,
) : FolderRepository {

    override fun observeAll(): Flow<List<Folder>> =
        dao.observeAll().map { entities -> entities.map(FolderEntity::toDomain) }

    override suspend fun upsert(folder: Folder): Long = dao.upsert(folder.toEntity())

    override suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }
}

private fun FolderEntity.toDomain(): Folder =
    Folder(id = id, name = name, orderIndex = orderIndex, createdAt = createdAt)

private fun Folder.toEntity(): FolderEntity =
    FolderEntity(id = id, name = name, orderIndex = orderIndex, createdAt = createdAt)
