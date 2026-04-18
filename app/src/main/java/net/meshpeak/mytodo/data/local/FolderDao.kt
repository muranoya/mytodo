package net.meshpeak.mytodo.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {
    @Query("SELECT * FROM folder ORDER BY order_index ASC, id ASC")
    fun observeAll(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folder WHERE id = :id")
    fun observe(id: Long): Flow<FolderEntity?>

    @Query("SELECT * FROM folder WHERE id = :id")
    suspend fun findById(id: Long): FolderEntity?

    @Query("SELECT COUNT(*) FROM folder")
    suspend fun count(): Int

    @Query("SELECT COALESCE(MAX(order_index), -1) FROM folder")
    suspend fun maxOrderIndex(): Int

    @Upsert
    suspend fun upsert(folder: FolderEntity): Long

    @Query("UPDATE folder SET order_index = :orderIndex WHERE id = :id")
    suspend fun setOrderIndex(id: Long, orderIndex: Int)

    @Transaction
    suspend fun updateOrderIndices(updates: List<FolderOrderUpdate>) {
        for (u in updates) setOrderIndex(u.id, u.orderIndex)
    }

    @Query("DELETE FROM folder WHERE id = :id")
    suspend fun deleteById(id: Long)
}
