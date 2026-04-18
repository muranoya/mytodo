package net.meshpeak.mytodo.domain.model

import java.time.Instant

data class Folder(
    val id: Long,
    val name: String,
    val orderIndex: Int,
    val createdAt: Instant,
)
