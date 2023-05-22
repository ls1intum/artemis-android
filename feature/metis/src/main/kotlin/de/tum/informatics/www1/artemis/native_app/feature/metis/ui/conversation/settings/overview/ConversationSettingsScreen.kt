package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.settings.overview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.NavigationBackButton
import de.tum.informatics.www1.artemis.native_app.feature.metis.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.conversationNavGraphBuilderExtension
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.overview.ConversationOverviewRoute
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.settings.add_members.navigateToAddMembersScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.settings.members.navigateToConversationMembersScreen
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavController.navigateToConversationSettingsScreen(
    courseId: Long,
    conversationId: Long,
    builder: NavOptionsBuilder.() -> Unit
) {
    navigate("course/$courseId/conversations/$conversationId/settings", builder)
}

fun NavGraphBuilder.conversationSettingsScreen(
    navController: NavController,
    onNavigateBack: () -> Unit
) {
    conversationNavGraphBuilderExtension(
        route = "course/{courseId}/conversations/{conversationId}/settings",
        deepLink = "artemis://courses/{courseId}/conversations/{conversationId}/settings"
    ) { courseId, conversationId ->
        ConversationSettingsScreen(
            modifier = Modifier.fillMaxSize(),
            courseId = courseId,
            conversationId = conversationId,
            onNavigateBack = onNavigateBack,
            onRequestAddMembers = {
                navController.navigateToAddMembersScreen(courseId, conversationId) { }
            },
            onRequestViewAllMembers = {
                navController.navigateToConversationMembersScreen(courseId, conversationId) {}
            },
            onConversationLeft = {
                navController.popBackStack(ConversationOverviewRoute, false)
            }
        )
    }
}

@Composable
private fun ConversationSettingsScreen(
    modifier: Modifier,
    courseId: Long,
    conversationId: Long,
    onNavigateBack: () -> Unit,
    onRequestAddMembers: () -> Unit,
    onRequestViewAllMembers: () -> Unit,
    onConversationLeft: () -> Unit
) {
    val viewModel: ConversationSettingsViewModel =
        koinViewModel { parametersOf(courseId, conversationId) }

    LaunchedEffect(key1 = Unit) {
        viewModel.requestReload()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.conversation_settings_title))
                },
                navigationIcon = {
                    NavigationBackButton(onNavigateBack)
                },
                actions = {
                    IconButton(onClick = viewModel::requestReload) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                    }
                }
            )
        }
    ) { paddingValues ->
        ConversationSettingsBody(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            viewModel = viewModel,
            onRequestViewAllMembers = onRequestViewAllMembers,
            onRequestAddMembers = onRequestAddMembers,
            onConversationLeft = onConversationLeft
        )
    }
}
