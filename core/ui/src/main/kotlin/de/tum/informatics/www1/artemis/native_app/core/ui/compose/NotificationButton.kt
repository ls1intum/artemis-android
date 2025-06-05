package de.tum.informatics.www1.artemis.native_app.core.ui.compose

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable

@Composable
fun NotificationButton(
    onNavigateNotificationView: (() -> Unit),
) {
    IconButton(
        onClick = { onNavigateNotificationView() },
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = null
        )
    }
} 