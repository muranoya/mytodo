package net.meshpeak.mytodo.domain.repository

import kotlinx.coroutines.flow.Flow
import net.meshpeak.mytodo.domain.model.Folder

interface FolderRepository {
    fun observeAll(): Flow<List<Folder>>

    fun observe(id: Long): Flow<Folder?>

    suspend fun findById(id: Long): Folder?

    suspend fun count(): Int

    /**
     * 新規は [Folder.id] が 0、編集は 1 以上。戻り値は自動採番された ID（編集時は同じ ID）。
     * 新規作成時は呼び出し側で `orderIndex` を指定していない場合に限り末尾へ追加する。
     */
    suspend fun upsert(folder: Folder): Long

    suspend fun deleteById(id: Long)

    suspend fun reorder(updates: List<Pair<Long, Int>>)
}
