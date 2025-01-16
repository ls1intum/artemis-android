package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.common

import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.ICourseUser
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.CourseUserRoleBadgesRow
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture.ProfilePictureWithDialog

@Composable
fun CourseUserListItem(
    modifier: Modifier = Modifier,
    user: ICourseUser,
    trailingContent: @Composable (() -> Unit)? = null
) {
    ListItem(
        modifier = modifier,
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
        leadingContent = {
            ProfilePictureWithDialog(
                courseUser = user
            )
        },
        trailingContent = trailingContent,
    )
}