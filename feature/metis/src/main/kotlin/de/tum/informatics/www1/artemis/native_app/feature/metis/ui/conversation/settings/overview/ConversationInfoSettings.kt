package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.settings.overview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.feature.metis.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.GroupChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.OneToOneChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.PotentiallyIllegalTextField
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.humanReadableName
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
            style = ConversationSettingsSectionTextStyle
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