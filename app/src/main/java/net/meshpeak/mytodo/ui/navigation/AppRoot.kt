package net.meshpeak.mytodo.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import net.meshpeak.mytodo.ui.components.AboutDialog
import net.meshpeak.mytodo.ui.folder.FolderDetailScreen
import net.meshpeak.mytodo.ui.folder.FolderListScreen
import net.meshpeak.mytodo.ui.overview.OverviewScreen
import net.meshpeak.mytodo.ui.trash.TrashScreen

@Composable
fun AppRoot() {
    val controller = rememberNavController()
    val backStackEntry by controller.currentBackStackEntryAsState()
    val showTopBar = backStackEntry?.destination?.isTopLevel() == true
    var showAbout by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (showTopBar) {
                TopLevelAppBar(onAboutClick = { showAbout = true })
            }
        },
        bottomBar = { MytodoBottomBar(controller) },
    ) { innerPadding ->
        NavHost(
            navController = controller,
            startDestination = TopRoute.FolderList,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable<TopRoute.Overview> { OverviewScreen() }
            composable<TopRoute.FolderList> {
                FolderListScreen(
                    onOpenFolder = { folderId ->
                        controller.navigate(TopRoute.FolderDetail(folderId))
                    },
                )
            }
            composable<TopRoute.FolderDetail> {
                FolderDetailScreen(onBack = { controller.popBackStack() })
            }
            composable<TopRoute.Trash> { TrashScreen() }
        }
    }

    if (showAbout) {
        AboutDialog(onDismiss = { showAbout = false })
    }
}

private fun NavDestination.isTopLevel(): Boolean =
    hasRoute<TopRoute.Overview>() ||
        hasRoute<TopRoute.FolderList>() ||
        hasRoute<TopRoute.Trash>()
