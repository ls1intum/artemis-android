package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.detail

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

fun NavController.navigateToConversationDetailScreen(courseId: Long, conversationId: Long, builder: NavOptionsBuilder.() -> Unit) {
    navigate("course/$courseId/conversations/$conversationId", builder)
}

fun NavGraphBuilder.conversationOverviewScreen(
    navController: NavController,
    onNavigateBack: () -> Unit
) {
    composable(
        route = "course/{courseId}/conversations/{conversationId}",
        arguments = listOf(
            navArgument("courseId") { type = NavType.LongType; nullable = false },
            navArgument("conversationId") { type = NavType.LongType; nullable = false }
        ),
        deepLinks = listOf(
            navDeepLink {
                uriPattern = "artemis://courses/{courseId}/conversations/{conversationId}"
            }
        )
    ) { backStackEntry ->
        val courseId =
            backStackEntry.arguments?.getLong("courseId")

        val conversationId =
            backStackEntry.arguments?.getLong("conversationId")

        checkNotNull(courseId)
        checkNotNull(conversationId)

        ConversationScreen(
            modifier = Modifier.fillMaxSize(),
            onNavigateToConversation = { conversation ->

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