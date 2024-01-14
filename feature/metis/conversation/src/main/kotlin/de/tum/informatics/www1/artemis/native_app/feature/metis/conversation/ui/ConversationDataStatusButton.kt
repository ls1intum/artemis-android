package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable

@Composable
internal fun ConversationDataStatusButton(
    dataStatus: DataStatus,
    onRequestSoftReload: () -> Unit,
    onRequestHardReload: () -> Unit
) {
    IconButton(onClick = { /*TODO*/ }) {
        Icon(
            imageVector = when (dataStatus) {
                DataStatus.Loading -> Icons.Default.CloudSync
                DataStatus.Outdated -> Icons.Default.SyncProblem
                DataStatus.UpToDate -> Icons.Default.CloudDone
            },
            contentDescription = null
        )
    }
}