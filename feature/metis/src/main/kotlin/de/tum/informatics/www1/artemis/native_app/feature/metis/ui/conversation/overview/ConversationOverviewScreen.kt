package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.overview

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicHintTextField
import de.tum.informatics.www1.artemis.native_app.feature.metis.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.conversation.ConversationCollection
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.browse_channels.navigateToBrowseChannelsScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.create_channel.navigateToCreateChannelScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.create_personal_conversation.navigateToCreatePersonalConversationScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.detail.navigateToConversationDetailScreen
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

const val ConversationOverviewRoute = "course/{courseId}/conversations"

fun NavController.navigateToConversationOverviewScreen(
    courseId: Long,
    builder: NavOptionsBuilder.() -> Unit
) {
    navigate("course/$courseId/conversations", builder)
}

fun NavGraphBuilder.conversationOverviewScreen(
    navController: NavController,
    onNavigateBack: () -> Unit
) {
    composable(
        route = ConversationOverviewRoute,
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
            courseId = courseId,
            onNavigateToConversation = { conversationId ->
                navController.navigateToConversationDetailScreen(courseId, conversationId) {}
            },
            onNavigateBack = onNavigateBack,
            onRequestCreatePersonalConversation = {
                navController.navigateToCreatePersonalConversationScreen(courseId) {}
            },
            onRequestAddChannel = {
                navController.navigateToBrowseChannelsScreen(courseId) {}
            }
        )
    }
}

@Composable
private fun ConversationScreen(
    modifier: Modifier,
    courseId: Long,
    onNavigateToConversation: (conversationId: Long) -> Unit,
    onRequestCreatePersonalConversation: () -> Unit,
    onRequestAddChannel: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val viewModel: ConversationOverviewViewModel = koinViewModel { parametersOf(courseId) }
    val conversationCollectionDataState: DataState<ConversationCollection> by viewModel.conversations.collectAsState()

    val query by viewModel.query.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.requestReload()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.conversation_overview_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::requestReload) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { padding ->
        BasicDataStateUi(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            dataState = conversationCollectionDataState,
            loadingText = stringResource(id = R.string.conversation_overview_loading),
            failureText = stringResource(id = R.string.conversation_overview_loading_failed),
            retryButtonText = stringResource(id = R.string.conversation_overview_loading_try_again),
            onClickRetry = viewModel::requestReload
        ) { conversationCollection ->
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ConversationSearch(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    query = query,
                    updateQuery = viewModel::onUpdateQuery
                )

                ConversationList(
                    modifier = Modifier
                        .fillMaxSize(),
                    conversationCollection = conversationCollection,
                    onNavigateToConversation = onNavigateToConversation,
                    onToggleMarkAsFavourite = viewModel::markConversationAsFavorite,
                    onToggleHidden = viewModel::markConversationAsHidden,
                    onRequestCreatePersonalConversation = onRequestCreatePersonalConversation,
                    onRequestAddChannel = onRequestAddChannel
                )
            }
        }
    }
}

@Composable
private fun ConversationSearch(
    modifier: Modifier,
    query: String,
    updateQuery: (String) -> Unit
) {
    Box(
        modifier = modifier.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(10)
        )
    ) {
        BasicHintTextField(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            hint = stringResource(id = R.string.conversation_overview_search_hint),
            value = query,
            onValueChange = updateQuery,
            maxLines = 1
        )
    }
}
