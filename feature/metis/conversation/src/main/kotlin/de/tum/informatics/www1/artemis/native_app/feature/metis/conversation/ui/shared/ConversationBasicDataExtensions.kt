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