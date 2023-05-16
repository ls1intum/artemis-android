package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.feature.metis.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.GroupChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.OneToOneChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.humanReadableName
import kotlinx.datetime.toJavaInstant
import java.text.SimpleDateFormat
import java.util.Date

@Composable
internal fun ConversationInfoSettings(
    modifier: Modifier,
    conversation: Conversation,
    name: String,
    description: String,
    topic: String,
    updateName: (String) -> Unit,
    updateDescription: (String) -> Unit,
    updateTopic: (String) -> Unit
) {
    Column(modifier = modifier) {
        when (conversation) {
            is ChannelChat -> TODO()
            is GroupChat -> TODO()
            is OneToOneChat -> {
                // No editable data
            }
        }

        SectionMoreInfo(modifier = Modifier.fillMaxWidth(), conversation = conversation)
    }
}

@Composable
private fun SectionMoreInfo(modifier: Modifier, conversation: Conversation) {
    val creator = conversation.creator
    val creatorText: Pair<String, String>? = remember(creator) {
        if (creator != null) {
            creator.humanReadableName to creator.username.orEmpty()
        } else null
    }

    val creationDate = conversation.creationDate
    val creationDateText: String? = remember(creationDate) {
        if (creationDate != null) {
            SimpleDateFormat
                .getDateTimeInstance(SimpleDateFormat.LONG, SimpleDateFormat.SHORT)
                .format(Date.from(creationDate.toJavaInstant()))
        } else null
    }

    Column(modifier = modifier) {
        Text(
            text = stringResource(id = R.string.conversation_settings_section_more_info),
            style = MaterialTheme.typography.titleSmall
        )

        if (creatorText != null) {
            Text(
                text = stringResource(
                    id = R.string.conversation_settings_section_more_info_created_by,
                    creatorText.first,
                    creatorText.second
                )
            )
        }

        if (creationDateText != null) {
            Text(
                text = stringResource(
                    id = R.string.conversation_settings_section_more_info_created_on,
                    creationDateText
                )
            )
        }
    }
}