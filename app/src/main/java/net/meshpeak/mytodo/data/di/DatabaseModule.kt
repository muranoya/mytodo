package net.meshpeak.mytodo.data.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import net.meshpeak.mytodo.data.local.FolderDao
import net.meshpeak.mytodo.data.local.MytodoDatabase
import net.meshpeak.mytodo.data.local.TodoDao

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): MytodoDatabase = Room.databaseBuilder(
        context,
        MytodoDatabase::class.java,
        MytodoDatabase.NAME,
    ).build()

    @Provides
    fun provideFolderDao(database: MytodoDatabase): FolderDao = database.folderDao()

    @Provides
    fun provideTodoDao(database: MytodoDatabase): TodoDao = database.todoDao()
}
