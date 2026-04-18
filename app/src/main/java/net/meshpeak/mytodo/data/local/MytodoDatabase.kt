package net.meshpeak.mytodo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [FolderEntity::class, TodoEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class MytodoDatabase : RoomDatabase() {
    abstract fun folderDao(): FolderDao

    abstract fun todoDao(): TodoDao

    companion object {
        const val NAME: String = "mytodo.db"
    }
}
