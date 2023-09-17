package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val channelNamePattern = "^[a-z0-9-]{1}[a-z0-9-]{0,20}\$".toRegex()

internal fun Flow<String>.mapIsChannelNameIllegal(): Flow<Boolean> =
    map { it.isNotEmpty() && !channelNamePattern.matches(it) }

internal fun Flow<String>.mapIsDescriptionOrTopicIllegal(): Flow<Boolean> =
    map { it.length > 250 }

@Composable
internal fun isReplyEnabled(conversationDataState: DataState<Conversation>): Boolean {
    val isReplyEnabled by remember(conversationDataState) {
        derivedStateOf {
            conversationDataState.bind { conversation ->
                when (conversation) {
                    is ChannelChat -> !conversation.isArchived
                    else -> true
                }
            }.orElse(false)
        }
    }

    return isReplyEnabled
}