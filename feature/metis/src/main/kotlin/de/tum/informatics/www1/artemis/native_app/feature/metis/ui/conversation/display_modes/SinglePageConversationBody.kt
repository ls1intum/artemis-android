package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.display_modes

import android.os.Parcelable
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.ui.getWindowSizeClass
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.browse_channels.BrowseChannelsScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.create_channel.CreateChannelScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.create_personal_conversation.CreatePersonalConversationScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.detail.ConversationScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.overview.ConversationOverviewBody
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.settings.add_members.ConversationAddMembersScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.settings.members.ConversationMembersScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.settings.overview.ConversationSettingsScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.view_post.MetisStandalonePostScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.view_post.StandalonePostId
import kotlinx.parcelize.Parcelize

private const val ConversationOverviewMaxWeight = 0.3f
private val ConversationOverviewMaxWidth = 600.dp

@Composable
fun SinglePageConversationBody(
    modifier: Modifier,
    courseId: Long,
    initialConfiguration: ConversationConfiguration = NothingOpened
) {
    var configuration: ConversationConfiguration by rememberSaveable(initialConfiguration) {
        mutableStateOf(initialConfiguration)
    }

    val openConversation = { conversationId: Long ->
        configuration = when (val currentConfig = configuration) {
            is OpenedConversation -> OpenedConversation(conversationId, null)
            else -> OpenedConversation(conversationId, null)
        }
    }

    BackHandler(configuration != NothingOpened) {
        when (val config = configuration) {
            is ConversationSettings -> configuration = config.prevConfiguration
            is AddChannelConfiguration -> configuration = config.prevConfiguration
            is CreatePersonalConversation -> configuration = config.prevConfiguration
            is OpenedConversation -> configuration =
                if (config.openedThread != null) config.copy(openedThread = null) else NothingOpened
            NothingOpened -> {}
        }
    }

    val ConversationOverview: @Composable (Modifier) -> Unit = { m ->
        ConversationOverviewBody(
            modifier = m.padding(top = 16.dp),
            courseId = courseId,
            onNavigateToConversation = openConversation,
            onRequestCreatePersonalConversation = {
                configuration = CreatePersonalConversation(configuration)
            },
            onRequestAddChannel = {
                configuration = AddChannelConfiguration(false, configuration)
            }
        )
    }

    val StandalonePost: @Composable (Modifier, OpenedConversation, OpenedThread) -> Unit =
        { m, parent, openedThread ->
            val metisContext = remember(openedThread) {
                MetisContext.Conversation(courseId, openedThread.conversationId)
            }

            MetisStandalonePostScreen(
                modifier = m,
                standalonePostId = openedThread.postId,
                metisContext = metisContext,
                onNavigateUp = {
                    configuration = OpenedConversation(parent.conversationId, null)
                }
            )
        }

    val ConversationDetails: @Composable (Modifier, OpenedConversation, canNavigateBack: Boolean) -> Unit =
        { m, conf, canNavigateBack ->
            ConversationScreen(
                modifier = m,
                courseId = courseId,
                conversationId = conf.conversationId,
                onNavigateBack = if (canNavigateBack) {
                    {
                        configuration = NothingOpened
                    }
                } else null,
                onNavigateToSettings = {
                    configuration = ConversationSettings(conf.conversationId, conf)
                },
                onClickViewPost = {
                    configuration = OpenedConversation(
                        conf.conversationId,
                        OpenedThread(conf.conversationId, StandalonePostId.ClientSideId(it))
                    )
                }
            )
        }

    when (val config = configuration) {
        NothingOpened -> {
            ConversationOverview(modifier)
        }

        is OpenedConversation -> {
            val widthSizeClass = getWindowSizeClass().widthSizeClass

            when {
                widthSizeClass <= WindowWidthSizeClass.Compact -> {
                    if (config.openedThread != null) {
                        StandalonePost(
                            modifier,
                            config,
                            config.openedThread
                        )
                    } else {
                        ConversationDetails(
                            modifier,
                            config,
                            true
                        )
                    }
                }

                else -> {
                    val arrangement = Arrangement.spacedBy(8.dp)

                    Row(
                        modifier = modifier,
                        horizontalArrangement = arrangement
                    ) {
                        val isOverviewVisible =
                            config.openedThread == null || widthSizeClass >= WindowWidthSizeClass.Expanded
                        AnimatedVisibility(
                            modifier = Modifier
                                .weight(ConversationOverviewMaxWeight)
                                .widthIn(max = ConversationOverviewMaxWidth)
                                .fillMaxHeight(),
                            visible = isOverviewVisible
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = arrangement
                            ) {
                                ConversationOverview(
                                    Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                )

                                VerticalDivider()
                            }
                        }

                        val otherWeight = when {
                            isOverviewVisible && config.openedThread != null -> 0.35f
                            isOverviewVisible && config.openedThread == null -> 0.7f
                            else -> 0.5f
                        }

                        val otherModifier = Modifier
                            .weight(otherWeight)
                            .fillMaxHeight()

                        ConversationDetails(
                            otherModifier,
                            config,
                            false
                        )

                        if (config.openedThread != null) {
                            VerticalDivider()

                            StandalonePost(
                                otherModifier,
                                config,
                                config.openedThread
                            )
                        }
                    }
                }
            }
        }

        is AddChannelConfiguration -> {
            if (config.isCreatingChannel) {
                CreateChannelScreen(
                    modifier = modifier,
                    courseId = courseId,
                    onConversationCreated = openConversation,
                    onNavigateBack = {
                        configuration = AddChannelConfiguration(false, config.prevConfiguration)
                    }
                )
            } else {
                BrowseChannelsScreen(
                    modifier = modifier,
                    courseId = courseId,
                    onNavigateToConversation = openConversation,
                    onNavigateToCreateChannel = {
                        configuration = AddChannelConfiguration(true, config.prevConfiguration)
                    },
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
                            configuration = config.copy(isAddingMembers = true, prevConfiguration = configuration)
                        },
                        onRequestViewAllMembers = {
                            configuration = config.copy(isViewingAllMembers = true, prevConfiguration = configuration)
                        },
                        onConversationLeft = {
                            configuration = NothingOpened
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(1.dp)
            .background(DividerDefaults.color)
    )
}

@Parcelize
sealed interface ConversationConfiguration : Parcelable

@Parcelize
object NothingOpened : ConversationConfiguration

@Parcelize
data class OpenedConversation(val conversationId: Long, val openedThread: OpenedThread?) :
    ConversationConfiguration

@Parcelize
data class OpenedThread(val conversationId: Long, val postId: StandalonePostId) : Parcelable

@Parcelize
private data class AddChannelConfiguration(
    val isCreatingChannel: Boolean,
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