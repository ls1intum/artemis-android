package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.browse_channels

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.ui.AwaitDeferredCompletion
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.alert.TextAlertDialog
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.NavigationBackButton
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.ChannelChatIcon
import kotlinx.coroutines.Deferred
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

internal fun testTagForBrowsedChannelItem(channelId: Long) = "browsedChannel$channelId"

@Composable
fun BrowseChannelsScreen(
    modifier: Modifier,
    courseId: Long,
    onNavigateToConversation: (conversationId: Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    BrowseChannelsScreen(
        modifier = modifier,
        viewModel = koinViewModel { parametersOf(courseId) },
        onNavigateToConversation = onNavigateToConversation,
        onNavigateBack = onNavigateBack
    )
}

@Composable
internal fun BrowseChannelsScreen(
    modifier: Modifier,
    viewModel: BrowseChannelsViewModel,
    onNavigateToConversation: (conversationId: Long) -> Unit,
    onNavigateBack: () -> Unit
) {

    LaunchedEffect(Unit) {
        viewModel.requestReload()
    }

    val channelsDataState by viewModel.channels.collectAsState()

    var registerInChannelJob: Deferred<Long?>? by remember { mutableStateOf(null) }
    var displayRegistrationFailedDialog by remember { mutableStateOf(false) }

    AwaitDeferredCompletion(
        job = registerInChannelJob,
        onComplete = { conversationId ->
            registerInChannelJob = null

            if (conversationId != null) {
                onNavigateToConversation(conversationId)
            } else {
                displayRegistrationFailedDialog = true
            }
        }
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            Surface(
                shadowElevation = Spacings.AppBarElevation
            ){
                TopAppBar(
                    title = { Text(text = stringResource(id = R.string.browse_channels_title)) },
                    navigationIcon = { NavigationBackButton(onNavigateBack) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                )
            }
        },

    ) { padding ->
        BasicDataStateUi(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .consumeWindowInsets(WindowInsets.systemBars.only(WindowInsetsSides.Top))
                .padding(horizontal = Spacings.ScreenHorizontalSpacing),
            dataState = channelsDataState,
            loadingText = stringResource(id = R.string.browse_channel_list_loading),
            failureText = stringResource(id = R.string.browse_channel_list_failure),
            retryButtonText = stringResource(id = R.string.browse_channel_list_try_again),
            onClickRetry = viewModel::requestReload
        ) { channels ->
            if (channels.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = Spacings.calculateContentPaddingValues()
                ) {
                    items(channels) { channelChat ->
                        ChannelChatItem(
                            modifier = Modifier.fillMaxWidth(),
                            channelChat = channelChat,
                            onClick = {
                                if (registerInChannelJob == null) {
                                    registerInChannelJob = viewModel.registerInChannel(channelChat)
                                }
                            }
                        )
                    }
                }
            } else {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = stringResource(id = R.string.browse_channel_list_empty),
                    textAlign = TextAlign.Center
                )
            }
        }

        if (displayRegistrationFailedDialog) {
            TextAlertDialog(
                title = stringResource(id = R.string.browse_channel_registration_failed_dialog_title),
                text = stringResource(id = R.string.browse_channel_registration_failed_dialog_message),
                confirmButtonText = stringResource(id = R.string.browse_channel_registration_failed_dialog_positive),
                dismissButtonText = null,
                onPressPositiveButton = { displayRegistrationFailedDialog = false },
                onDismissRequest = { displayRegistrationFailedDialog = false }
            )
        }
    }
}

@Composable
private fun ChannelChatItem(
    modifier: Modifier = Modifier,
    channelChat: ChannelChat,
    onClick: () -> Unit
) {
    ListItem(
        modifier = modifier,
        leadingContent = {
            ChannelChatIcon(channelChat = channelChat)
        },
        headlineContent = { Text(channelChat.name) },
        supportingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (channelChat.isMember) {
                    Text(
                        text = stringResource(id = R.string.joined_channel),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                shape = MaterialTheme.shapes.extraSmall
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Text(
                    text = pluralStringResource(
                        id = R.plurals.browse_channel_channel_item_member_count,
                        count = channelChat.numberOfMembers,
                        channelChat.numberOfMembers
                    )
                )
            }
        },
        trailingContent = {
            if (!channelChat.isMember) {
                Button(
                    modifier = Modifier.testTag(testTagForBrowsedChannelItem(channelChat.id)),
                    onClick = onClick,
                ) {
                    Text(text = stringResource(id = R.string.join_button_title))
                }
            }
        }
    )
}
