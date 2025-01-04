package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.SavedPostStatus

@Composable
fun SavedPostStatus.getIcon(): ImageVector {
    return when (this) {
        SavedPostStatus.IN_PROGRESS -> Icons.Default.Bookmark
        SavedPostStatus.COMPLETED -> Icons.Default.CheckBox
        SavedPostStatus.ARCHIVED -> Icons.Default.Archive
    }
}

@Composable
fun SavedPostStatus.getUiText(): String {
    return stringResource(id = this.getStringResId())
}

fun SavedPostStatus.getStringResId(): Int {
    return when (this) {
        SavedPostStatus.IN_PROGRESS -> R.string.saved_posts_in_progress
        SavedPostStatus.COMPLETED -> R.string.saved_posts_completed
        SavedPostStatus.ARCHIVED -> R.string.saved_posts_archived
    }
}