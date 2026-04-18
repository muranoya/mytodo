package net.meshpeak.mytodo.domain.repository

import kotlinx.coroutines.flow.Flow
import net.meshpeak.mytodo.domain.model.Folder

interface FolderRepository {
    fun observeAll(): Flow<List<Folder>>

    suspend fun upsert(folder: Folder): Long

    suspend fun deleteById(id: Long)
}
