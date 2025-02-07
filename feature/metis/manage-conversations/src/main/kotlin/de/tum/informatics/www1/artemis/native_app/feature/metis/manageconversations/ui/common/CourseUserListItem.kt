package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.common

import androidx.compose.foundation.layout.size
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.ICourseUser
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.CourseUserRoleBadgesRow
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture.ProfilePictureWithDialog

// From https://m3.material.io/components/lists/specs#d156b3f2-6763-4fde-ba6f-0f088ce5a4e4
private val LIST_ITEM_LEADING_AVATAR_HEIGHT = 40.dp


@Composable
fun CourseUserListItem(
    modifier: Modifier = Modifier,
    user: ICourseUser,
    trailingContent: @Composable (() -> Unit)? = null
) {
    ListItem(
        modifier = modifier,
        leadingContent = {
            ProfilePictureWithDialog(
                modifier = Modifier.size(LIST_ITEM_LEADING_AVATAR_HEIGHT),
                courseUser = user
            )
        },
        headlineContent = {
            CourseUserRoleBadgesRow(
                user = user
            )
        },
        supportingContent = user.name?.let {
            {
                Text(
                    text = it,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        trailingContent = trailingContent,
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        )
    )
}