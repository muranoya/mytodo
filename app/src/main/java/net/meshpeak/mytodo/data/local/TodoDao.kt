package net.meshpeak.mytodo.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    @Query(
        """
        SELECT * FROM todo
        WHERE is_completed = 0 AND deleted_at IS NULL
        ORDER BY priority_rank ASC, created_at ASC, id ASC
        """,
    )
    fun observeActive(): Flow<List<TodoEntity>>

    @Query(
        """
        SELECT * FROM todo
        WHERE folder_id = :folderId AND is_completed = 0 AND deleted_at IS NULL
        ORDER BY priority_rank ASC, order_index ASC, id ASC
        """,
    )
    fun observeByFolder(folderId: Long): Flow<List<TodoEntity>>

    @Query(
        """
        SELECT * FROM todo
        WHERE is_completed = 1 OR deleted_at IS NOT NULL
        ORDER BY COALESCE(deleted_at, completed_at) DESC, id DESC
        """,
    )
    fun observeTrashed(): Flow<List<TodoEntity>>

    @Upsert
    suspend fun upsert(todo: TodoEntity): Long

    @Query("DELETE FROM todo WHERE id = :id")
    suspend fun deleteById(id: Long)
}
