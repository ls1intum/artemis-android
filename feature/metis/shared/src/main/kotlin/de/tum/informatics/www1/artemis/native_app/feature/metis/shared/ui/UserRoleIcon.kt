package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.UserRole
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ConversationUser


@Composable
fun ConversationUserRoleIndicators(
    modifier: Modifier = Modifier,
    user: ConversationUser,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        UserRoleIcon(
            modifier = modifier,
            userRole = user.getUserRole()
        )

        if (user.isChannelModerator) {
            Icon(
                modifier = modifier,
                imageVector = Icons.Default.Shield,
                contentDescription = stringResource(id = R.string.user_role_icon_content_description_moderator)
            )
        }
    }
}

@Composable
fun UserRoleIcon(
    modifier: Modifier = Modifier,
    userRole: UserRole?,
) {
    val icon = when (userRole) {
        UserRole.INSTRUCTOR -> Icons.Default.School
        UserRole.TUTOR -> Icons.Default.SupervisorAccount
        UserRole.USER -> Icons.Default.Person
        null -> Icons.Default.Person
    }

    val contentDescription = when(userRole) {
        UserRole.INSTRUCTOR -> R.string.user_role_icon_content_description_instructor
        UserRole.TUTOR -> R.string.user_role_icon_content_description_teaching_assistant
        UserRole.USER -> R.string.user_role_icon_content_description_student
        null -> R.string.user_role_icon_content_description_student
    }

    Box {
        Icon(
            modifier = modifier
                .size(16.dp),
            imageVector = icon,
            contentDescription = stringResource(id = contentDescription)
        )
    }
}