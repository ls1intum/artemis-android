package de.tum.informatics.www1.artemis.native_app.feature.metis.ui

import android.os.Parcelable
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.AccountDataService
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.CourseService
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.account.isAtLeastTutorInCourse
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.ConversationScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.browse_channels.BrowseChannelsScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.create_channel.CreateChannelScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.create_personal_conversation.CreatePersonalConversationScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview.ConversationOverviewBody
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.add_members.ConversationAddMembersScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.members.ConversationMembersScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.overview.ConversationSettingsScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.StandalonePostId
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

@Composable
internal fun SinglePageConversationBody(
    modifier: Modifier,
    courseId: Long,
    initialConfiguration: ConversationConfiguration = NothingOpened,
    accountService: AccountService,
    serverConfigurationService: ServerConfigurationService,
    courseService: CourseService,
    accountDataService: AccountDataService,
    networkStatusProvider: NetworkStatusProvider
) {
    var configuration: ConversationConfiguration by rememberSaveable(initialConfiguration) {
        mutableStateOf(initialConfiguration)
    }

    val openConversation = { conversationId: Long ->
        configuration = when (configuration) {
            is OpenedConversation -> OpenedConversation(conversationId, null)
            else -> OpenedConversation(conversationId, null)
        }
    }

    var canCreateChannel by rememberSaveable { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(courseId) {
        coroutineScope.launch {
            val flow = flatMapLatest(
                serverConfigurationService.serverUrl,
                accountService.authToken
            ) { serverUrl, authToken ->
                retryOnInternet(networkStatusProvider.currentNetworkStatus) {
                    courseService.getCourse(courseId, serverUrl, authToken)
                        .then { courseWithScore ->
                            accountDataService
                                .getAccountData(serverUrl, authToken)
                                .bind { it.isAtLeastTutorInCourse(courseWithScore.course) }
                        }
                }.map { it.orElse(false) }
            }

            flow.collect { value ->
                canCreateChannel = value
            }
        }
    }

    BackHandler(configuration != NothingOpened) {
        when (val config = configuration) {
            is ConversationSettings -> configuration = config.prevConfiguration
            is AddChannelConfiguration -> configuration = config.prevConfiguration
            is BrowseChannelConfiguration -> configuration = config.prevConfiguration
            is CreatePersonalConversation -> configuration = config.prevConfiguration
            is OpenedConversation -> configuration =
                if (config.openedThread != null) config.copy(openedThread = null) else NothingOpened

            is NavigateToUserConversation -> configuration = NothingOpened
            NothingOpened -> {}
        }
    }

    val conversationOverview: @Composable (Modifier) -> Unit = { m ->
        ConversationOverviewBody(
            modifier = m.padding(top = 16.dp),
            courseId = courseId,
            onNavigateToConversation = openConversation,
            onRequestCreatePersonalConversation = {
                configuration = CreatePersonalConversation(configuration)
            },
            onRequestAddChannel = {
                if (canCreateChannel) {
                    configuration = AddChannelConfiguration(configuration)
                }
            },
            onRequestBrowseChannel = {
                configuration = BrowseChannelConfiguration(configuration)
            },
            canCreateChannel = canCreateChannel
        )
    }

    when (val config = configuration) {
        NothingOpened -> {
            conversationOverview(modifier)
        }

        is OpenedConversation -> {
            ConversationScreen(
                modifier = modifier,
                conversationId = config.conversationId,
                threadPostId = config.openedThread?.postId,
                courseId = courseId,
                onOpenThread = { postId ->
                    configuration = OpenedConversation(
                        config.conversationId,
                        OpenedThread(config.conversationId, postId)
                    )
                },
                onCloseThread = {
                    configuration = config.copy(openedThread = null)
                },
                onCloseConversation = {
                    configuration = NothingOpened
                },
                onNavigateToSettings = {
                    configuration = ConversationSettings(
                        conversationId = config.conversationId,
                        prevConfiguration = config
                    )
                },
                conversationsOverview = { mod -> conversationOverview(mod) }
            )
        }

        is BrowseChannelConfiguration -> {
            BrowseChannelsScreen(
                modifier = modifier,
                courseId = courseId,
                onNavigateToConversation = openConversation,
                //onNavigateToCreateChannel = {},
                onNavigateBack = { configuration = config.prevConfiguration }
            )
        }

        is AddChannelConfiguration -> {
            if (canCreateChannel) {
                CreateChannelScreen(
                    modifier = modifier,
                    courseId = courseId,
                    onConversationCreated = openConversation,
                    onNavigateBack = { configuration = config.prevConfiguration }
                )
            }
        }

        is CreatePersonalConversation -> {
            CreatePersonalConversationScreen(
                modifier = modifier,
                courseId = courseId,
                onConversationCreated = openConversation,
                onNavigateBack = { configuration = config.prevConfiguration }
            )
        }

        is ConversationSettings -> {
            when {
                config.isViewingAllMembers -> {
                    ConversationMembersScreen(
                        modifier = modifier,
                        courseId = courseId,
                        conversationId = config.conversationId,
                        onNavigateBack = { configuration = config.prevConfiguration }
                    )
                }

                config.isAddingMembers -> {
                    ConversationAddMembersScreen(
                        modifier = modifier,
                        courseId = courseId,
                        onNavigateBack = { configuration = config.prevConfiguration }
                    )
                }

                else -> {
                    ConversationSettingsScreen(
                        modifier = modifier,
                        courseId = courseId,
                        conversationId = config.conversationId,
                        onNavigateBack = { configuration = config.prevConfiguration },
                        onRequestAddMembers = {
                            configuration = config.copy(
                                isAddingMembers = true,
                                prevConfiguration = configuration
                            )
                        },
                        onRequestViewAllMembers = {
                            configuration = config.copy(
                                isViewingAllMembers = true,
                                prevConfiguration = configuration
                            )
                        },
                        onConversationLeft = {
                            configuration = NothingOpened
                        }
                    )
                }
            }
        }

        is NavigateToUserConversation -> {
            NavigateToUserConversationUi(
                modifier = modifier,
                courseId = courseId,
                username = config.username,
                onNavigateToConversation = { conversationId ->
                    configuration = OpenedConversation(conversationId, null)
                },
                onNavigateBack = { configuration = NothingOpened }
            )
        }
    }
}


@Parcelize
sealed interface ConversationConfiguration : Parcelable

@Parcelize
data object NothingOpened : ConversationConfiguration

@Parcelize
data class OpenedConversation(val conversationId: Long, val openedThread: OpenedThread?) :
    ConversationConfiguration

@Parcelize
data class OpenedThread(val conversationId: Long, val postId: StandalonePostId) : Parcelable

/**
 * Special configuration in which we want to navigate to the 1-to-1 conversation with the user with the specified username.
 * In this configuration, we simply show a loading bar while we load the necessary data to show the chat.
 */
@Parcelize
data class NavigateToUserConversation(val username: String) : ConversationConfiguration

@Parcelize
private data class AddChannelConfiguration(
    val prevConfiguration: ConversationConfiguration
) :
    ConversationConfiguration

@Parcelize
private data class BrowseChannelConfiguration(
    val prevConfiguration: ConversationConfiguration
) :
    ConversationConfiguration

@Parcelize
private data class CreatePersonalConversation(val prevConfiguration: ConversationConfiguration) :
    ConversationConfiguration

@Parcelize
private data class ConversationSettings(
    val conversationId: Long,
    val prevConfiguration: ConversationConfiguration,
    val isAddingMembers: Boolean = false,
    val isViewingAllMembers: Boolean = false
) : ConversationConfiguration
