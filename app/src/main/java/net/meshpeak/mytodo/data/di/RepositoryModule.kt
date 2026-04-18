package net.meshpeak.mytodo.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import net.meshpeak.mytodo.data.repository.FolderRepositoryImpl
import net.meshpeak.mytodo.data.repository.TodoRepositoryImpl
import net.meshpeak.mytodo.domain.repository.FolderRepository
import net.meshpeak.mytodo.domain.repository.TodoRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindFolderRepository(impl: FolderRepositoryImpl): FolderRepository

    @Binds
    @Singleton
    abstract fun bindTodoRepository(impl: TodoRepositoryImpl): TodoRepository
}
