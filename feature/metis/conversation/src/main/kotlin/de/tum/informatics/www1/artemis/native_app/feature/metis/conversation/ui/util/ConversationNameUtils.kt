package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.humanReadableName

@Composable
fun rememberDerivedConversationName(conversation: DataState<Conversation>): State<String> {
    val fallbackName = stringResource(R.string.conversation_name_fallback)
    return remember(conversation) {
        derivedStateOf {
            conversation.bind { it.humanReadableName }.orElse(fallbackName)
        }
    }
}