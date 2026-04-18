package net.meshpeak.mytodo.data.repository

import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.meshpeak.mytodo.data.local.TodoDao
import net.meshpeak.mytodo.data.local.TodoEntity
import net.meshpeak.mytodo.data.local.TodoOrderUpdate
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

    override fun observeActiveCountPerFolder(): Flow<Map<Long, Int>> =
        dao.observeActiveCountPerFolder().map { rows ->
            rows.associate { it.folderId to it.activeCount }
        }

    override suspend fun findById(id: Long): Todo? = dao.findById(id)?.toDomain()

    override suspend fun upsert(todo: Todo): Long {
        val entity = if (todo.id == 0L && todo.orderIndex < 0) {
            val nextOrder = dao.maxOrderIndexInFolder(todo.folderId) + 1
            todo.copy(orderIndex = nextOrder).toEntity()
        } else {
            todo.toEntity()
        }
        return dao.upsert(entity)
    }

    override suspend fun setCompleted(id: Long, isCompleted: Boolean, completedAt: Instant?) {
        dao.setCompleted(id, isCompleted, completedAt)
    }

    override suspend fun setDeletedAt(id: Long, deletedAt: Instant?) {
        dao.setDeletedAt(id, deletedAt)
    }

    override suspend fun restore(id: Long) {
        dao.restore(id)
    }

    override suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }

    override suspend fun purgeOlderThan(threshold: Instant): Int = dao.purgeOlderThan(threshold)

    override suspend fun purgeAllTrashed(): Int = dao.purgeAllTrashed()

    override suspend fun reorder(updates: List<Pair<Long, Int>>) {
        dao.updateOrderIndices(updates.map { (id, idx) -> TodoOrderUpdate(id, idx) })
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
