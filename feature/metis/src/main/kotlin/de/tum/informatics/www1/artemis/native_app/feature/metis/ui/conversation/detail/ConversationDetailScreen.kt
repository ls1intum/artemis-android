package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon.Companion.Text
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.conversationNavGraphBuilderExtension
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.settings.navigateToConversationSettingsScreen

fun NavController.navigateToConversationDetailScreen(
    courseId: Long,
    conversationId: Long,
    builder: NavOptionsBuilder.() -> Unit
) {
    navigate("course/$courseId/conversations/$conversationId", builder)
}

fun NavGraphBuilder.conversationDetailScreen(
    navController: NavController,
    onNavigateBack: () -> Unit
) {
    conversationNavGraphBuilderExtension(
        route = "course/{courseId}/conversations/{conversationId}",
        deepLink = "artemis://courses/{courseId}/conversations/{conversationId}"
    ) { courseId, conversationId ->
        ConversationScreen(
            modifier = Modifier.fillMaxSize(),
            onNavigateBack = onNavigateBack,
            onNavigateToSettings = {
                navController.navigateToConversationSettingsScreen(courseId, conversationId) {}
            }
        )
    }
}

@Composable
private fun ConversationScreen(
    modifier: Modifier,
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text("Conversation")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->

    }
}