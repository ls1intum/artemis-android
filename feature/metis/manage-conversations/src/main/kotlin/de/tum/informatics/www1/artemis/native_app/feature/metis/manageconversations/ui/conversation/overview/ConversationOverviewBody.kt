package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicHintTextField
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ConversationCollections
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.R
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ConversationOverviewBody(
    modifier: Modifier,
    courseId: Long,
    onNavigateToConversation: (conversationId: Long) -> Unit,
    onRequestCreatePersonalConversation: () -> Unit,
    onRequestAddChannel: () -> Unit
) {
    ConversationOverviewBody(
        modifier = modifier,
        viewModel = koinViewModel { parametersOf(courseId) },
        onNavigateToConversation = onNavigateToConversation,
        onRequestCreatePersonalConversation = onRequestCreatePersonalConversation,
        onRequestAddChannel = onRequestAddChannel
    )
}

@Composable
fun ConversationOverviewBody(
    modifier: Modifier,
    viewModel: ConversationOverviewViewModel,
    onNavigateToConversation: (conversationId: Long) -> Unit,
    onRequestCreatePersonalConversation: () -> Unit,
    onRequestAddChannel: () -> Unit
) {
    val conversationCollectionsDataState: DataState<ConversationCollections> by viewModel.conversations.collectAsState()

    val isConnected by viewModel.isConnected.collectAsState()

    val query by viewModel.query.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.requestReload()
    }

    BasicDataStateUi(
        modifier = modifier,
        dataState = conversationCollectionsDataState,
        loadingText = stringResource(id = R.string.conversation_overview_loading),
        failureText = stringResource(id = R.string.conversation_overview_loading_failed),
        retryButtonText = stringResource(id = R.string.conversation_overview_loading_try_again),
        onClickRetry = viewModel::requestReload
    ) { conversationCollection ->
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AnimatedVisibility(modifier = Modifier.fillMaxWidth(), visible = !isConnected) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.WifiOff, contentDescription = null)

                        Text(
                            text = stringResource(id = R.string.conversation_overview_not_connected_banner),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

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
                viewModel = viewModel,
                conversationCollections = conversationCollection,
                onNavigateToConversation = { conversationId ->
                    viewModel.setConversationMessagesRead(conversationId)
                    onNavigateToConversation(conversationId)
                },
                onToggleMarkAsFavourite = viewModel::markConversationAsFavorite,
                onToggleHidden = viewModel::markConversationAsHidden,
                onRequestCreatePersonalConversation = onRequestCreatePersonalConversation,
                onRequestAddChannel = onRequestAddChannel
            )
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
