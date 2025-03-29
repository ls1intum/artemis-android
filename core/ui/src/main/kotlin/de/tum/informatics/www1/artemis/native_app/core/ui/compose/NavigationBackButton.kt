package de.tum.informatics.www1.artemis.native_app.core.ui.compose

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun NavigationBackButton(
    onNavigateBack: (() -> Unit)? = null,
    imageVector: ImageVector = Icons.AutoMirrored.Filled.ArrowBack
) {
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    IconButton(onClick = {
        onNavigateBack?.invoke() ?: backDispatcher?.onBackPressed() // Use function if provided, otherwise system back
    }) {
        Icon(imageVector = imageVector, contentDescription = "Back")
    }
}
