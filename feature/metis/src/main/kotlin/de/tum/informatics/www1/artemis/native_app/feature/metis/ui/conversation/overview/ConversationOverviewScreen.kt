package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.overview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.detail.navigateToConversationDetailScreen

fun NavController.navigateToConversationOverviewScreen(courseId: Long, builder: NavOptionsBuilder.() -> Unit) {
    navigate("course/$courseId/conversations", builder)
}

fun NavGraphBuilder.conversationOverviewScreen(
    navController: NavController,
    onNavigateBack: () -> Unit
) {
    composable(
        route = "course/{courseId}/conversations",
        arguments = listOf(
            navArgument("courseId") { type = NavType.LongType; nullable = false }
        ),
        deepLinks = listOf(
            navDeepLink {
                uriPattern = "artemis://courses/{courseId}/conversations"
            }
        )
    ) { backStackEntry ->
        val courseId =
            backStackEntry.arguments?.getLong("courseId")
        checkNotNull(courseId)

        ConversationScreen(
            modifier = Modifier.fillMaxSize(),
            onNavigateToConversation = { conversation ->
                navController.navigateToConversationDetailScreen(courseId, conversation.id) {}
            },
            onNavigateBack = onNavigateBack
        )
    }
}

@Composable
private fun ConversationScreen(
    modifier: Modifier,
    onNavigateToConversation: (Conversation) -> Unit,
    onNavigateBack: () -> Unit
) {

}