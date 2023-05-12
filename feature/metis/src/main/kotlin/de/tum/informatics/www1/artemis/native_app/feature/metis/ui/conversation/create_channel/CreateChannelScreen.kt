package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.create_channel

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.NavigationBackButton
import de.tum.informatics.www1.artemis.native_app.feature.metis.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.detail.navigateToConversationDetailScreen

fun NavController.navigateToCreateChannelScreen(
    courseId: Long,
    builder: NavOptionsBuilder.() -> Unit
) {
    navigate("course/$courseId/create_channel", builder)
}

fun NavGraphBuilder.createChannelScreen(
    navController: NavController,
    onNavigateBack: () -> Unit
) {
    composable(
        route = "course/{courseId}/create_channel",
        arguments = listOf(
            navArgument("courseId") { type = NavType.LongType; nullable = false }
        )
    ) { backStackEntry ->
        val courseId =
            backStackEntry.arguments?.getLong("courseId")
        checkNotNull(courseId)

        CreateChannelScreen(
            modifier = Modifier.fillMaxSize(),
            courseId = courseId,
            onConversationCreated = { conversationId ->
                navController.popBackStack()
                navController.navigateToConversationDetailScreen(courseId, conversationId) { }
            },
            onNavigateBack = onNavigateBack
        )
    }
}

@Composable
private fun CreateChannelScreen(
    modifier: Modifier,
    courseId: Long,
    onConversationCreated: (conversationId: Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.create_channel_title))
                },
                navigationIcon = {
                    NavigationBackButton(onNavigateBack)
                }
            )
        }
    ) { paddingValues ->

    }
}