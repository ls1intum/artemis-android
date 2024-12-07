package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.UserRole

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

    Box {
        Icon(
            modifier = modifier
                .size(16.dp),
            imageVector = icon,
            contentDescription = null
        )
    }
}