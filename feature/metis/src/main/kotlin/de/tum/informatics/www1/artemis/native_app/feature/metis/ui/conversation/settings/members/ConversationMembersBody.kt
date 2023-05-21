package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.settings.members

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import de.tum.informatics.www1.artemis.native_app.core.data.join
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.feature.metis.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.settings.ConversationMemberListItem
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.settings.PerformActionOnUserData
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.settings.PerformActionOnUserDialogs
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.settings.UserAction
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun ConversationMembersBody(
    modifier: Modifier,
    courseId: Long,
    conversationId: Long
) {
    val viewModel: ConversationMembersViewModel =
        koinViewModel { parametersOf(courseId, conversationId) }

    val query by viewModel.query.collectAsState()

    val members = viewModel.membersPagingData.collectAsLazyPagingItems()

    val clientUsername by viewModel.clientUsername.collectAsState()
    val conversation by viewModel.conversation.collectAsState()

    var userActionData: PerformActionOnUserData? by remember { mutableStateOf(null) }

    BasicDataStateUi(
        modifier = modifier,
        dataState = conversation join clientUsername,
        loadingText = stringResource(id = R.string.conversation_members_loading),
        failureText = stringResource(id = R.string.conversation_members_failure),
        retryButtonText = stringResource(id = R.string.conversation_members_try_again),
        onClickRetry = viewModel::requestReload
    ) { (conversation, clientUsername) ->
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacings.ScreenHorizontalSpacing),
                value = query,
                onValueChange = viewModel::updateQuery,
                placeholder = { Text(text = stringResource(id = R.string.conversation_members_query_placeholder)) }
            )

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(members) { conversationUser ->
                    if (conversationUser != null) {
                        ConversationMemberListItem(
                            member = conversationUser,
                            clientUsername = clientUsername,
                            conversation = conversation,
                            onRequestKickMember = {
                                userActionData = PerformActionOnUserData(it, UserAction.KICK)
                            },
                            onRequestGrantModerationPermission = {
                                userActionData =
                                    PerformActionOnUserData(it, UserAction.GIVE_MODERATION_RIGHTS)
                            },
                            onRequestRevokeModerationPermission = {
                                userActionData =
                                    PerformActionOnUserData(it, UserAction.REVOKE_MODERATION_RIGHTS)
                            }
                        )
                    }
                }

                when (members.loadState.append) {
                    LoadState.Loading -> {
                        item {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = Spacings.ScreenHorizontalSpacing)
                            )
                        }
                    }

                    is LoadState.Error -> {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = Spacings.ScreenHorizontalSpacing),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.conversation_members_failure),
                                    textAlign = TextAlign.Center
                                )

                                TextButton(onClick = members::retry) {
                                    Text(text = stringResource(id = R.string.conversation_members_try_again))
                                }
                            }
                        }
                    }

                    is LoadState.NotLoading -> {}
                }
            }
        }

        PerformActionOnUserDialogs(
            conversation = conversation,
            performActionOnUserData = userActionData,
            viewModel = viewModel,
            onDismiss = { userActionData = null }
        )
    }
}