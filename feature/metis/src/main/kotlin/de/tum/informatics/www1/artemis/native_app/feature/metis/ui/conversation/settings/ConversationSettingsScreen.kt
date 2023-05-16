package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.conversationNavGraphBuilderExtension

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

    }
}