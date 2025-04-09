package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.overview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.ui.common.ArtemisSection
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.common.PotentiallyIllegalTextField
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.GroupChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.OneToOneChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.humanReadableName
import kotlinx.datetime.toJavaInstant
import java.text.SimpleDateFormat
import java.util.Date

@Composable
internal fun ConversationInfoSettings(
    modifier: Modifier,
    conversation: Conversation,
    editableConversationInfo: EditableConversationInfo
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (conversation) {
            is ChannelChat -> {
                SectionBasicData(
                    modifier = Modifier.fillMaxWidth(),
                    editableConversationInfo = editableConversationInfo,
                    displayDescription = true,
                    displayTopic = true
                )
            }

            is GroupChat -> {
                SectionBasicData(
                    modifier = Modifier.fillMaxWidth(),
                    editableConversationInfo = editableConversationInfo,
                    displayDescription = false,
                    displayTopic = false
                )
            }

            is OneToOneChat -> {
                // No editable data
            }
        }
    }
}

/**
 * Inspect and edit name, description, topic
 */
@Composable
private fun SectionBasicData(
    modifier: Modifier,
    editableConversationInfo: EditableConversationInfo,
    displayDescription: Boolean,
    displayTopic: Boolean,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val textFieldModifier = Modifier.fillMaxWidth()

        PotentiallyIllegalTextField(
            modifier = textFieldModifier,
            label = R.string.conversation_settings_basic_data_name,
            value = editableConversationInfo.name,
            placeholder = R.string.conversation_settings_basic_data_name_empty,
            updateValue = editableConversationInfo.updateName,
            isIllegal = editableConversationInfo.isNameIllegal,
            illegalStateExplanation = R.string.channel_text_field_name_invalid,
            requiredSupportText = null,
            readOnly = !editableConversationInfo.canEditName
        )

        if (displayDescription) {
            PotentiallyIllegalTextField(
                modifier = textFieldModifier,
                label = R.string.conversation_settings_basic_data_description,
                value = editableConversationInfo.description,
                placeholder = R.string.conversation_settings_basic_data_description_empty,
                updateValue = editableConversationInfo.updateDescription,
                isIllegal = editableConversationInfo.isDescriptionIllegal,
                illegalStateExplanation = R.string.channel_text_field_description_invalid,
                requiredSupportText = null,
                readOnly = !editableConversationInfo.canEditDescription
            )
        }

        if (displayTopic) {
            PotentiallyIllegalTextField(
                modifier = textFieldModifier,
                label = R.string.conversation_settings_basic_data_topic,
                value = editableConversationInfo.topic,
                placeholder = R.string.conversation_settings_basic_data_topic_empty,
                updateValue = editableConversationInfo.updateTopic,
                isIllegal = editableConversationInfo.isTopicIllegal,
                illegalStateExplanation = R.string.channel_text_field_topic_invalid,
                requiredSupportText = null,
                readOnly = !editableConversationInfo.canEditTopic
            )
        }

        AnimatedVisibility(visible = editableConversationInfo.isDirty || editableConversationInfo.isSavingChanges) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = editableConversationInfo.onRequestSaveChanges,
                enabled = !editableConversationInfo.isSavingChanges && editableConversationInfo.canSave
            ) {
                Text(text = stringResource(id = R.string.conversation_settings_basic_data_save))

                if (editableConversationInfo.isSavingChanges) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
internal fun SectionMoreInfo(modifier: Modifier, conversation: Conversation) {
    val creator = conversation.creator
    val creatorText: String? = remember(creator) { creator?.humanReadableName }

    val creationDate = conversation.creationDate
    val creationDateText: String? = remember(creationDate) {
        if (creationDate != null) {
            SimpleDateFormat
                .getDateTimeInstance(SimpleDateFormat.LONG, SimpleDateFormat.SHORT)
                .format(Date.from(creationDate.toJavaInstant()))
        } else null
    }

    ArtemisSection(
        modifier = modifier,
        title = stringResource(id = R.string.conversation_settings_section_more_info)
    ) {
        if (creatorText != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.conversation_settings_section_more_info_created_by),
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = creatorText,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        if (creationDateText != null) {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.conversation_settings_section_more_info_created_on),
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = creationDateText,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}