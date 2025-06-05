package de.tum.informatics.www1.artemis.native_app.feature.coursenotifications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Text
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.NavigationBackButton
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun CourseNotificationScreen(
    modifier: Modifier = Modifier,
    courseId: Long,
    onNavigateBack: () -> Unit
) {
    val viewModel: CourseNotificationViewModel = koinViewModel { parametersOf(courseId) }
    var selectedFilter by remember { mutableStateOf(NotificationFilter.COMMUNICATION) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    NavigationBackButton(onNavigateBack = onNavigateBack)
                },
                title = {
                    Text(text = stringResource(id = R.string.notifications_title))
                },
                actions = {
                    IconButton({}) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(id = R.string.settings)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            FilterBar(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it }
            )

            NotificationList(
                modifier = Modifier.fillMaxSize(),
                filter = selectedFilter,
                viewModel = viewModel
            )
        }
    }
}

@Composable
private fun FilterBar(
    selectedFilter: NotificationFilter,
    onFilterSelected: (NotificationFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        NotificationFilter.entries.forEach { filter ->
            FilterChip(
                selected = filter == selectedFilter,
                onClick = { onFilterSelected(filter) },
                label = {
                    Text(
                        text = stringResource(
                            id = when (filter) {
                                NotificationFilter.COMMUNICATION -> R.string.communication
                                NotificationFilter.GENERAL -> R.string.general
                            }
                        )
                    )
                }
            )
        }
    }
}

@Composable
private fun NotificationList(
    modifier: Modifier,
    filter: NotificationFilter,
    viewModel: CourseNotificationViewModel
) {
    val notifications by when (filter) {
        NotificationFilter.COMMUNICATION -> viewModel.communicationNotifications.collectAsState()
        NotificationFilter.GENERAL -> viewModel.generalNotifications.collectAsState()
    }

    if (notifications.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(id = R.string.no_notifications),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(notifications) { notification ->
                NotificationItem(notification = notification)
            }
        }
    }
}

@Composable
private fun NotificationItem(
    notification: CourseNotification
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = notification.notification.notificationPlaceholders.firstOrNull() ?: "",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = notification.notification.type.toString(),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

enum class NotificationFilter {
    COMMUNICATION,
    GENERAL
}
