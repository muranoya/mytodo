package net.meshpeak.mytodo.domain.model

import java.time.Instant

data class Folder(
    val id: Long = 0L,
    val name: String,
    val orderIndex: Int = UNASSIGNED_ORDER,
    val createdAt: Instant = Instant.EPOCH,
) {
    companion object {
        /** Repository.upsert が「末尾に追加」と解釈するセンチネル値。 */
        const val UNASSIGNED_ORDER: Int = -1
    }
}
