package net.meshpeak.mytodo.ui.navigation

import kotlinx.serialization.Serializable

sealed interface TopRoute {
    @Serializable
    data object Overview : TopRoute

    @Serializable
    data object FolderList : TopRoute

    @Serializable
    data class FolderDetail(val folderId: Long) : TopRoute

    @Serializable
    data object Trash : TopRoute
}
