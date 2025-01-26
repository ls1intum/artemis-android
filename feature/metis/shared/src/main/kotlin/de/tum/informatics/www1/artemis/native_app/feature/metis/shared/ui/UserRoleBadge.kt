package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.ui.material.colors.PostColors
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.ICourseUser
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.UserRole
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ConversationUser


val DEFAULT_USER_ROLE = UserRole.USER

private sealed class ExtendedUserRole {
    data object Instructor : ExtendedUserRole()
    data object Tutor : ExtendedUserRole()
    data object Student : ExtendedUserRole()
    data object Moderator : ExtendedUserRole()
}


@Composable
fun CourseUserRoleBadgesRow(
    modifier: Modifier = Modifier,
    user: ICourseUser
) {
    val roles = mutableListOf(user.getUserRole().toExtendedUserRole())
    if (user is ConversationUser && user.isChannelModerator) roles.add(ExtendedUserRole.Moderator)

    UserRoleBadgesRow(
        modifier = modifier,
        roles = roles
    )
}

@Composable
private fun UserRoleBadgesRow(
    modifier: Modifier = Modifier,
    roles: List<ExtendedUserRole>,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        roles.forEach { role ->
            UserRoleBadge(extendedUserRole = role)
        }
    }
}


@Composable
fun UserRoleBadge(
    modifier: Modifier = Modifier,
    userRole: UserRole?
) {
    UserRoleBadge(
        modifier = modifier,
        extendedUserRole = userRole.toExtendedUserRole()
    )
}


@Composable
private fun UserRoleBadge(
    modifier: Modifier = Modifier,
    extendedUserRole: ExtendedUserRole
) {
    val text = extendedUserRole.getRoleText()

    Row(
        modifier = modifier
            .background(
                color = extendedUserRole.getBackgroundColor(),
                shape = MaterialTheme.shapes.extraSmall
            )
            .padding(horizontal = 6.dp, vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            modifier = Modifier.size(12.dp),
            imageVector = extendedUserRole.getIcon(),
            tint = Color.White,
            contentDescription = text
        )

        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}


private fun UserRole?.toExtendedUserRole(): ExtendedUserRole = when (this) {
    UserRole.INSTRUCTOR -> ExtendedUserRole.Instructor
    UserRole.TUTOR -> ExtendedUserRole.Tutor
    UserRole.USER -> ExtendedUserRole.Student
    null -> DEFAULT_USER_ROLE.toExtendedUserRole()
}

private fun ExtendedUserRole.getIcon(): ImageVector = when (this) {
    ExtendedUserRole.Instructor -> Icons.Default.School
    ExtendedUserRole.Tutor -> Icons.Default.SupervisorAccount
    ExtendedUserRole.Student -> Icons.Default.Person
    ExtendedUserRole.Moderator -> Icons.Default.Shield
}

@Composable
private fun ExtendedUserRole.getRoleText(): String = stringResource(when (this) {
    ExtendedUserRole.Instructor -> R.string.user_role_instructor
    ExtendedUserRole.Tutor -> R.string.user_role_tutor
    ExtendedUserRole.Student -> R.string.user_role_student
    ExtendedUserRole.Moderator -> R.string.user_role_moderator
})

@Composable
private fun ExtendedUserRole.getBackgroundColor(): Color = when (this) {
    ExtendedUserRole.Instructor -> PostColors.Roles.instructor
    ExtendedUserRole.Tutor -> PostColors.Roles.tutor
    ExtendedUserRole.Student -> PostColors.Roles.student
    ExtendedUserRole.Moderator -> PostColors.Roles.moderator
}

@Preview
@Composable
private fun UserRoleBadgePreview() {
    MaterialTheme {
        UserRoleBadgesRow(
            roles = listOf(
                ExtendedUserRole.Instructor,
                ExtendedUserRole.Tutor,
                ExtendedUserRole.Student,
                ExtendedUserRole.Moderator
            )
        )
    }
}