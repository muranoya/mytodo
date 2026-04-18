package net.meshpeak.mytodo.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import net.meshpeak.mytodo.R

private data class TopTab(
    val route: TopRoute,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
)

private val tabs: List<TopTab> = listOf(
    TopTab(TopRoute.FolderList, R.string.nav_folders, Icons.Filled.Folder),
    TopTab(TopRoute.Overview, R.string.nav_overview, Icons.Filled.Inbox),
    TopTab(TopRoute.Trash, R.string.nav_trash, Icons.Filled.DeleteSweep),
)

@Composable
fun MytodoBottomBar(controller: NavHostController) {
    val backStackEntry by controller.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    NavigationBar {
        tabs.forEach { tab ->
            val selected = currentDestination?.isIn(tab.route) == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        controller.navigate(tab.route) {
                            popUpTo(controller.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(tab.icon, contentDescription = null) },
                label = { Text(stringResource(tab.labelRes)) },
            )
        }
    }
}

/**
 * `FolderList` タブは `FolderDetail` 画面もその傘下として扱う（バックスタック内で「フォルダ」タブがハイライト）。
 */
private fun NavDestination.isIn(route: TopRoute): Boolean = when (route) {
    is TopRoute.Overview -> hasRoute<TopRoute.Overview>()
    is TopRoute.FolderList -> hasRoute<TopRoute.FolderList>() || hasRoute<TopRoute.FolderDetail>()
    is TopRoute.FolderDetail -> hasRoute<TopRoute.FolderDetail>()
    is TopRoute.Trash -> hasRoute<TopRoute.Trash>()
}
