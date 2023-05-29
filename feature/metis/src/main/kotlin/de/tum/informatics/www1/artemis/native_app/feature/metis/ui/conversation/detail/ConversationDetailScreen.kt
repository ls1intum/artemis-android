package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.detail

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.conversationNavGraphBuilderExtension
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.settings.overview.navigateToConversationSettingsScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post_list.MetisChatList
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

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
            courseId = courseId,
            conversationId = conversationId,
            onNavigateToSettings = {
                navController.navigateToConversationSettingsScreen(courseId, conversationId) {}
            }
        )
    }
}

@Composable
private fun ConversationScreen(
    modifier: Modifier,
    courseId: Long,
    conversationId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val metisContext = remember(courseId, conversationId) {
        MetisContext.Conversation(courseId, conversationId)
    }

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
        MetisChatList(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            viewModel = koinViewModel { parametersOf(metisContext) },
            listContentPadding = PaddingValues()
        ) {}
    }
}