package net.meshpeak.mytodo.domain.repository

import java.time.Instant
import kotlinx.coroutines.flow.Flow
import net.meshpeak.mytodo.domain.model.Todo

interface TodoRepository {
    fun observeActive(): Flow<List<Todo>>

    fun observeByFolder(folderId: Long): Flow<List<Todo>>

    fun observeTrashed(): Flow<List<Todo>>

    fun observeActiveCountPerFolder(): Flow<Map<Long, Int>>

    suspend fun findById(id: Long): Todo?

    /**
     * 新規は [Todo.id] が 0、編集は 1 以上。戻り値は自動採番された ID（編集時は同じ ID）。
     * 新規作成時は呼び出し側で `orderIndex` を指定していない場合に限り、フォルダ内の末尾に追加する。
     */
    suspend fun upsert(todo: Todo): Long

    suspend fun setCompleted(id: Long, isCompleted: Boolean, completedAt: Instant?)

    suspend fun setDeletedAt(id: Long, deletedAt: Instant?)

    suspend fun restore(id: Long)

    suspend fun deleteById(id: Long)

    suspend fun purgeOlderThan(threshold: Instant): Int

    suspend fun purgeAllTrashed(): Int

    suspend fun reorder(updates: List<Pair<Long, Int>>)
}
