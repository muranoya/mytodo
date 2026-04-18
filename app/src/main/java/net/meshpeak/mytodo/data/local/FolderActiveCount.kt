package net.meshpeak.mytodo.data.local

import androidx.room.ColumnInfo

data class FolderActiveCount(
    @ColumnInfo(name = "folder_id") val folderId: Long,
    @ColumnInfo(name = "active_count") val activeCount: Int,
)
