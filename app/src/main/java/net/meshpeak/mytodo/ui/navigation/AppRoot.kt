package net.meshpeak.mytodo.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import net.meshpeak.mytodo.ui.folder.FolderScreen
import net.meshpeak.mytodo.ui.overview.OverviewScreen
import net.meshpeak.mytodo.ui.trash.TrashScreen

@Composable
fun AppRoot() {
    val controller = rememberNavController()
    Scaffold(
        bottomBar = { MytodoBottomBar(controller) },
    ) { innerPadding ->
        NavHost(
            navController = controller,
            startDestination = TopRoute.Overview,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable<TopRoute.Overview> { OverviewScreen() }
            composable<TopRoute.Folders> { FolderScreen() }
            composable<TopRoute.Trash> { TrashScreen() }
        }
    }
}
