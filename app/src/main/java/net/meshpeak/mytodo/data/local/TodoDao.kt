package net.meshpeak.mytodo.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import java.time.Instant
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

    @Query(
        """
        SELECT folder_id AS folder_id, COUNT(*) AS active_count
        FROM todo
        WHERE is_completed = 0 AND deleted_at IS NULL
        GROUP BY folder_id
        """,
    )
    fun observeActiveCountPerFolder(): Flow<List<FolderActiveCount>>

    @Query("SELECT * FROM todo WHERE id = :id")
    suspend fun findById(id: Long): TodoEntity?

    @Upsert
    suspend fun upsert(todo: TodoEntity): Long

    @Query(
        "UPDATE todo SET is_completed = :isCompleted, completed_at = :completedAt WHERE id = :id",
    )
    suspend fun setCompleted(id: Long, isCompleted: Boolean, completedAt: Instant?)

    @Query("UPDATE todo SET deleted_at = :deletedAt WHERE id = :id")
    suspend fun setDeletedAt(id: Long, deletedAt: Instant?)

    @Query(
        "UPDATE todo SET is_completed = 0, completed_at = NULL, deleted_at = NULL WHERE id = :id",
    )
    suspend fun restore(id: Long)

    @Query("DELETE FROM todo WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query(
        """
        DELETE FROM todo
        WHERE (is_completed = 1 OR deleted_at IS NOT NULL)
          AND COALESCE(deleted_at, completed_at) < :threshold
        """,
    )
    suspend fun purgeOlderThan(threshold: Instant): Int

    @Query("DELETE FROM todo WHERE is_completed = 1 OR deleted_at IS NOT NULL")
    suspend fun purgeAllTrashed(): Int

    @Query(
        """
        SELECT COALESCE(MAX(order_index), -1)
        FROM todo WHERE folder_id = :folderId
        """,
    )
    suspend fun maxOrderIndexInFolder(folderId: Long): Int

    @Query("UPDATE todo SET order_index = :orderIndex WHERE id = :id")
    suspend fun setOrderIndex(id: Long, orderIndex: Int)

    @Transaction
    suspend fun updateOrderIndices(updates: List<TodoOrderUpdate>) {
        for (u in updates) setOrderIndex(u.id, u.orderIndex)
    }
}
