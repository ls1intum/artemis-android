package de.tum.informatics.www1.artemis.native_app.core.ui.compose

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun NavigationBackButton(
    onNavigateBack: () -> Unit,
    imageVector: ImageVector = Icons.Default.ArrowBack
) {
    IconButton(onClick = onNavigateBack) {
        Icon(imageVector = imageVector, contentDescription = null)
    }
}
