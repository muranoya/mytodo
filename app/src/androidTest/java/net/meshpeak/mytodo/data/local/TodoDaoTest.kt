package net.meshpeak.mytodo.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TodoDaoTest {

    private lateinit var db: MytodoDatabase
    private lateinit var todoDao: TodoDao
    private lateinit var folderDao: FolderDao

    private val folderId: Long = 1L
    private val baseInstant: Instant = Instant.parse("2026-04-01T00:00:00Z")

    @Before
    fun setup() = runTest {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MytodoDatabase::class.java,
        ).allowMainThreadQueries().build()
        todoDao = db.todoDao()
        folderDao = db.folderDao()
        folderDao.upsert(
            FolderEntity(
                id = folderId,
                name = "テスト",
                orderIndex = 0,
                createdAt = baseInstant,
            ),
        )
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun observeActive_excludesCompletedAndDeleted_andOrdersByPriorityRank() = runTest {
        val active = todoEntity(id = 0, title = "active-today", priorityRank = 1)
        val soon = todoEntity(id = 0, title = "active-asap", priorityRank = 0)
        val completed = todoEntity(
            id = 0,
            title = "completed",
            priorityRank = 0,
            isCompleted = true,
            completedAt = baseInstant,
        )
        val deleted = todoEntity(
            id = 0,
            title = "deleted",
            priorityRank = 0,
            deletedAt = baseInstant,
        )
        todoDao.upsert(active)
        todoDao.upsert(soon)
        todoDao.upsert(completed)
        todoDao.upsert(deleted)

        val rows = todoDao.observeActive().first()
        assertEquals(listOf("active-asap", "active-today"), rows.map(TodoEntity::title))
    }

    @Test
    fun observeTrashed_returnsOnlyCompletedOrSoftDeleted() = runTest {
        todoDao.upsert(todoEntity(id = 0, title = "active"))
        todoDao.upsert(
            todoEntity(
                id = 0,
                title = "completed",
                isCompleted = true,
                completedAt = baseInstant,
            ),
        )
        todoDao.upsert(
            todoEntity(
                id = 0,
                title = "deleted",
                deletedAt = baseInstant.plusSeconds(10),
            ),
        )

        val trashed = todoDao.observeTrashed().first().map(TodoEntity::title)
        assertTrue(trashed.contains("completed"))
        assertTrue(trashed.contains("deleted"))
        assertTrue(!trashed.contains("active"))
    }

    @Test
    fun purgeOlderThan_deletesExpiredAndKeepsBoundary() = runTest {
        val threshold = Instant.parse("2026-03-01T00:00:00Z")
        todoDao.upsert(
            todoEntity(
                id = 0,
                title = "expired-deleted",
                deletedAt = threshold.minusSeconds(1),
            ),
        )
        todoDao.upsert(
            todoEntity(
                id = 0,
                title = "expired-completed",
                isCompleted = true,
                completedAt = threshold.minusSeconds(1),
            ),
        )
        todoDao.upsert(
            todoEntity(
                id = 0,
                title = "boundary",
                deletedAt = threshold,
            ),
        )
        todoDao.upsert(
            todoEntity(
                id = 0,
                title = "fresh-completed",
                isCompleted = true,
                completedAt = threshold.plusSeconds(1),
            ),
        )
        todoDao.upsert(todoEntity(id = 0, title = "active"))

        val removed = todoDao.purgeOlderThan(threshold)
        assertEquals(2, removed)

        val remaining = todoDao.observeTrashed().first().map(TodoEntity::title)
        assertEquals(setOf("boundary", "fresh-completed"), remaining.toSet())
    }

    private fun todoEntity(
        id: Long,
        title: String,
        priorityRank: Int = 2,
        isCompleted: Boolean = false,
        completedAt: Instant? = null,
        deletedAt: Instant? = null,
        createdAt: Instant = baseInstant,
    ): TodoEntity = TodoEntity(
        id = id,
        folderId = folderId,
        title = title,
        note = null,
        priorityRank = priorityRank,
        isCompleted = isCompleted,
        orderIndex = 0,
        createdAt = createdAt,
        completedAt = completedAt,
        deletedAt = deletedAt,
    )
}
