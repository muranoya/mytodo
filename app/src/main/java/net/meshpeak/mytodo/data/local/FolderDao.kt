package net.meshpeak.mytodo.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {
    @Query("SELECT * FROM folder ORDER BY order_index ASC, id ASC")
    fun observeAll(): Flow<List<FolderEntity>>

    @Upsert
    suspend fun upsert(folder: FolderEntity): Long

    @Query("DELETE FROM folder WHERE id = :id")
    suspend fun deleteById(id: Long)
}
