package net.meshpeak.mytodo.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "todo",
    foreignKeys = [
        ForeignKey(
            entity = FolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["folder_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("folder_id"),
        Index("priority_rank"),
        Index("deleted_at"),
        Index("is_completed"),
    ],
)
data class TodoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    @ColumnInfo(name = "folder_id")
    val folderId: Long,
    val title: String,
    val note: String?,
    @ColumnInfo(name = "priority_rank")
    val priorityRank: Int,
    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean,
    @ColumnInfo(name = "order_index")
    val orderIndex: Int,
    @ColumnInfo(name = "created_at")
    val createdAt: Instant,
    @ColumnInfo(name = "completed_at")
    val completedAt: Instant?,
    @ColumnInfo(name = "deleted_at")
    val deletedAt: Instant?,
)
