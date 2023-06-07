package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.browse_channels

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import de.tum.informatics.www1.artemis.native_app.core.ui.AwaitDeferredCompletion
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.alert.TextAlertDialog
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.NavigationBackButton
import de.tum.informatics.www1.artemis.native_app.feature.metis.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.common.ChannelIcons
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.courseNavGraphBuilderExtensions
import kotlinx.coroutines.Deferred
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

internal const val BrowseChannelsRoute = "course/{courseId}/browse_channel"

fun NavController.navigateToBrowseChannelsScreen(
    courseId: Long,
    builder: NavOptionsBuilder.() -> Unit
) {
    navigate("course/$courseId/browse_channel", builder)
}

fun NavGraphBuilder.browseChannelsScreen(
    onNavigateToConversation: (courseId: Long, conversationId: Long) -> Unit,
    onNavigateToCreateChannel: (courseId: Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    courseNavGraphBuilderExtensions(
        route = BrowseChannelsRoute
    ) { courseId ->
        BrowseChannelsScreen(
            modifier = Modifier.fillMaxSize(),
            courseId = courseId,
            onNavigateToConversation = { conversationId ->
                onNavigateToConversation(courseId, conversationId)
            },
            onNavigateToCreateChannel = { onNavigateToCreateChannel(courseId) },
            onNavigateBack = onNavigateBack
        )
    }
}

@Composable
internal fun BrowseChannelsScreen(
    modifier: Modifier,
    courseId: Long,
    onNavigateToConversation: (conversationId: Long) -> Unit,
    onNavigateToCreateChannel: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val viewModel: BrowseChannelsViewModel = koinViewModel { parametersOf(courseId) }

    val canCreateChannel: Boolean by viewModel.canCreateChannel.collectAsState()

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
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.browse_channels_title)) },
                navigationIcon = { NavigationBackButton(onNavigateBack) }
            )
        },
        floatingActionButton = {
            if (canCreateChannel) {
                FloatingActionButton(onClick = onNavigateToCreateChannel) {
                    Icon(imageVector = Icons.Default.Create, contentDescription = null)
                }
            }
        }
    ) { padding ->
        BasicDataStateUi(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            dataState = channelsDataState,
            loadingText = stringResource(id = R.string.browse_channel_list_loading),
            failureText = stringResource(id = R.string.browse_channel_list_failure),
            retryButtonText = stringResource(id = R.string.browse_channel_list_try_again),
            onClickRetry = viewModel::requestReload
        ) { channels ->
            if (channels.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = Spacings.FabContentBottomPadding)
                ) {
                    items(channels) { channelChat ->
                        ChannelChatItem(
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
private fun ChannelChatItem(channelChat: ChannelChat, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        leadingContent = {
            ChannelIcons(channelChat)
        },
        headlineContent = { Text(channelChat.name) },
        supportingContent = {
            Text(
                text = pluralStringResource(
                    id = R.plurals.browse_channel_channel_item_member_count,
                    count = channelChat.numberOfMembers,
                    channelChat.numberOfMembers
                )
            )
        }
    )
}
