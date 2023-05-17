package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon.Companion.Text
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.NavigationBackButton
import de.tum.informatics.www1.artemis.native_app.feature.metis.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.conversationNavGraphBuilderExtension
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
            onNavigateBack = onNavigateBack
        )
    }
}

@Composable
private fun ConversationSettingsScreen(
    modifier: Modifier,
    courseId: Long,
    conversationId: Long,
    onNavigateBack: () -> Unit
) {
    val viewModel: ConversationSettingsViewModel =
        koinViewModel { parametersOf(courseId, conversationId) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.conversation_settings_title))
                },
                navigationIcon = {
                    NavigationBackButton(onNavigateBack)
                }
            )
        }
    ) { paddingValues ->
        ConversationSettingsBody(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            viewModel = viewModel
        )
    }
}
