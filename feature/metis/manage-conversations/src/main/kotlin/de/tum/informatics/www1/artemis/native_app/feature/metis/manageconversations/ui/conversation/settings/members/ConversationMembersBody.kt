package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.members

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import de.tum.informatics.www1.artemis.native_app.core.data.join
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.ConversationMemberListItem
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.PerformActionOnUserData
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.PerformActionOnUserDialogs
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.UserAction
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ConversationUser
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.PagingStateError
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

internal const val TEST_TAG_MEMBERS_LIST = "TEST_TAG_MEMBERS_LIST"

internal fun testTagForMember(username: String) = "member$username"

@Composable
internal fun ConversationMembersBody(
    modifier: Modifier,
    courseId: Long,
    conversationId: Long,
    viewModel: ConversationMembersViewModel = koinViewModel {
        parametersOf(
            courseId,
            conversationId
        )
    }
) {
    LaunchedEffect(courseId, conversationId) {
        viewModel.updateConversation(courseId, conversationId)
    }

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

            ConversationMembersList(
                modifier = Modifier.fillMaxSize(),
                members = members,
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

        PerformActionOnUserDialogs(
            conversation = conversation,
            performActionOnUserData = userActionData,
            viewModel = viewModel,
            onDismiss = { userActionData = null }
        )
    }
}

@Composable
private fun ConversationMembersList(
    modifier: Modifier,
    members: LazyPagingItems<ConversationUser>,
    clientUsername: String,
    conversation: Conversation,
    onRequestKickMember: (ConversationUser) -> Unit,
    onRequestGrantModerationPermission: (ConversationUser) -> Unit,
    onRequestRevokeModerationPermission: (ConversationUser) -> Unit
) {
    when (members.loadState.refresh) {
        LoadState.Loading -> {
            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.conversation_members_loading),
                        textAlign = TextAlign.Center
                    )

                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        }

        is LoadState.Error -> {
            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center
            ) {
                PagingStateError(
                    modifier = Modifier,
                    errorText = R.string.conversation_members_failure,
                    buttonText = R.string.conversation_members_try_again,
                    retry = members::retry
                )
            }
        }

        is LoadState.NotLoading -> {
            LazyColumn(modifier = modifier.testTag(TEST_TAG_MEMBERS_LIST)) {
                items(
                    count = members.itemCount,
                    key = members.itemKey(key = { it.id })
                ) { index ->
                    val item = members[index]
                    if (item != null) {
                        ConversationMemberListItem(
                            modifier = Modifier.testTag(testTagForMember(item.username.orEmpty())),
                            member = item,
                            clientUsername = clientUsername,
                            conversation = conversation,
                            onRequestKickMember = onRequestKickMember,
                            onRequestGrantModerationPermission = onRequestGrantModerationPermission,
                            onRequestRevokeModerationPermission = onRequestRevokeModerationPermission
                        )
                    }
                }

                when (members.loadState.append) {
                    LoadState.Loading -> {
                        item {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    is LoadState.Error -> {
                        item {
                            PagingStateError(
                                modifier = Modifier.fillMaxWidth(),
                                errorText = R.string.conversation_members_failure,
                                buttonText = R.string.conversation_members_try_again,
                                retry = members::retry
                            )
                        }
                    }

                    is LoadState.NotLoading -> {}
                }
            }
        }
    }
}
