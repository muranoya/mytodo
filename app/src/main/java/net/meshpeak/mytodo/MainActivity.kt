package net.meshpeak.mytodo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import net.meshpeak.mytodo.ui.navigation.AppRoot
import net.meshpeak.mytodo.ui.theme.MytodoTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MytodoTheme {
                AppRoot()
            }
        }
    }
}
