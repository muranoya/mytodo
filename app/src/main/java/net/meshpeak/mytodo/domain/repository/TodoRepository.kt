package net.meshpeak.mytodo.domain.repository

import kotlinx.coroutines.flow.Flow
import net.meshpeak.mytodo.domain.model.Todo

interface TodoRepository {
    fun observeActive(): Flow<List<Todo>>

    fun observeByFolder(folderId: Long): Flow<List<Todo>>

    fun observeTrashed(): Flow<List<Todo>>

    suspend fun upsert(todo: Todo): Long

    suspend fun deleteById(id: Long)
}
