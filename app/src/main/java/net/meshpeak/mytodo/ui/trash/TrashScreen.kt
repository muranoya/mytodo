package net.meshpeak.mytodo.ui.trash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import net.meshpeak.mytodo.R
import net.meshpeak.mytodo.ui.theme.MytodoTheme

@Composable
fun TrashScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.placeholder_trash),
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

@Preview
@Composable
private fun TrashScreenPreview() {
    MytodoTheme(dynamicColor = false) {
        TrashScreen()
    }
}
