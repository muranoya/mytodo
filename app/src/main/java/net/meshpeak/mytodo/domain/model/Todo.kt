package net.meshpeak.mytodo.domain.model

import java.time.Instant

data class Todo(
    val id: Long,
    val folderId: Long,
    val title: String,
    val note: String?,
    val priority: Priority,
    val isCompleted: Boolean,
    val orderIndex: Int,
    val createdAt: Instant,
    val completedAt: Instant?,
    val deletedAt: Instant?,
)
