package net.meshpeak.mytodo.data.repository

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.meshpeak.mytodo.data.local.FolderDao
import net.meshpeak.mytodo.data.local.FolderEntity
import net.meshpeak.mytodo.data.local.FolderOrderUpdate
import net.meshpeak.mytodo.domain.model.Folder
import net.meshpeak.mytodo.domain.repository.FolderRepository

class FolderRepositoryImpl @Inject constructor(
    private val dao: FolderDao,
) : FolderRepository {

    override fun observeAll(): Flow<List<Folder>> =
        dao.observeAll().map { entities -> entities.map(FolderEntity::toDomain) }

    override fun observe(id: Long): Flow<Folder?> =
        dao.observe(id).map { it?.toDomain() }

    override suspend fun findById(id: Long): Folder? = dao.findById(id)?.toDomain()

    override suspend fun count(): Int = dao.count()

    override suspend fun upsert(folder: Folder): Long {
        val entity = if (folder.id == 0L && folder.orderIndex < 0) {
            val nextOrder = dao.maxOrderIndex() + 1
            folder.copy(orderIndex = nextOrder).toEntity()
        } else {
            folder.toEntity()
        }
        return dao.upsert(entity)
    }

    override suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }

    override suspend fun reorder(updates: List<Pair<Long, Int>>) {
        dao.updateOrderIndices(updates.map { (id, idx) -> FolderOrderUpdate(id, idx) })
    }
}

private fun FolderEntity.toDomain(): Folder =
    Folder(id = id, name = name, orderIndex = orderIndex, createdAt = createdAt)

private fun Folder.toEntity(): FolderEntity =
    FolderEntity(id = id, name = name, orderIndex = orderIndex, createdAt = createdAt)
