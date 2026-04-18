package net.meshpeak.mytodo.domain.model

import java.time.Instant

data class Todo(
    val id: Long = 0L,
    val folderId: Long,
    val title: String,
    val note: String? = null,
    val priority: Priority = Priority.Today,
    val isCompleted: Boolean = false,
    val orderIndex: Int = UNASSIGNED_ORDER,
    val createdAt: Instant = Instant.EPOCH,
    val completedAt: Instant? = null,
    val deletedAt: Instant? = null,
) {
    companion object {
        /** Repository.upsert が「フォルダ末尾に追加」と解釈するセンチネル値。 */
        const val UNASSIGNED_ORDER: Int = -1
    }
}
