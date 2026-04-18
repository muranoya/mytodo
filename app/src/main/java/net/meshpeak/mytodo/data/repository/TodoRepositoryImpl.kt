package net.meshpeak.mytodo.data.repository

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.meshpeak.mytodo.data.local.TodoDao
import net.meshpeak.mytodo.data.local.TodoEntity
import net.meshpeak.mytodo.domain.model.Priority
import net.meshpeak.mytodo.domain.model.Todo
import net.meshpeak.mytodo.domain.repository.TodoRepository

class TodoRepositoryImpl @Inject constructor(
    private val dao: TodoDao,
) : TodoRepository {

    override fun observeActive(): Flow<List<Todo>> =
        dao.observeActive().map { rows -> rows.map(TodoEntity::toDomain) }

    override fun observeByFolder(folderId: Long): Flow<List<Todo>> =
        dao.observeByFolder(folderId).map { rows -> rows.map(TodoEntity::toDomain) }

    override fun observeTrashed(): Flow<List<Todo>> =
        dao.observeTrashed().map { rows -> rows.map(TodoEntity::toDomain) }

    override suspend fun upsert(todo: Todo): Long = dao.upsert(todo.toEntity())

    override suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }
}

private fun TodoEntity.toDomain(): Todo = Todo(
    id = id,
    folderId = folderId,
    title = title,
    note = note,
    priority = Priority.fromRank(priorityRank),
    isCompleted = isCompleted,
    orderIndex = orderIndex,
    createdAt = createdAt,
    completedAt = completedAt,
    deletedAt = deletedAt,
)

private fun Todo.toEntity(): TodoEntity = TodoEntity(
    id = id,
    folderId = folderId,
    title = title,
    note = note,
    priorityRank = priority.rank,
    isCompleted = isCompleted,
    orderIndex = orderIndex,
    createdAt = createdAt,
    completedAt = completedAt,
    deletedAt = deletedAt,
)
