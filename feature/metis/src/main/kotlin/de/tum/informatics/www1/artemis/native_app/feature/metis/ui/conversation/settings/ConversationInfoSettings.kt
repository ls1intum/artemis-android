package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.settings

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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

        SectionMoreInfo(modifier = Modifier.fillMaxWidth(), conversation = conversation)
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

        BasicDataTextField(
            modifier = textFieldModifier,
            label = R.string.conversation_settings_basic_data_name,
            emptyHint = R.string.conversation_settings_basic_data_name_empty,
            value = editableConversationInfo.name,
            onValueChange = editableConversationInfo.updateName,
            canEdit = editableConversationInfo.canEditName
        )

        if (displayDescription) {
            BasicDataTextField(
                modifier = textFieldModifier,
                label = R.string.conversation_settings_basic_data_description,
                emptyHint = R.string.conversation_settings_basic_data_description_empty,
                value = editableConversationInfo.description,
                onValueChange = editableConversationInfo.updateDescription,
                canEdit = editableConversationInfo.canEditDescription
            )
        }

        if (displayTopic) {
            BasicDataTextField(
                modifier = textFieldModifier,
                label = R.string.conversation_settings_basic_data_topic,
                emptyHint = R.string.conversation_settings_basic_data_topic_empty,
                value = editableConversationInfo.topic,
                onValueChange = editableConversationInfo.updateTopic,
                canEdit = editableConversationInfo.canEditTopic
            )
        }

        AnimatedVisibility(visible = editableConversationInfo.isDirty || editableConversationInfo.isSavingChanges) {
            Button(
                onClick = editableConversationInfo.onRequestSaveChanges,
                enabled = !editableConversationInfo.isSavingChanges
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
private fun BasicDataTextField(
    modifier: Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    canEdit: Boolean,
    @StringRes label: Int,
    @StringRes emptyHint: Int
) {
    OutlinedTextField(
        modifier = modifier,
        label = { Text(text = stringResource(id = label)) },
        value = value,
        onValueChange = onValueChange,
        readOnly = !canEdit,
        placeholder = { Text(text = stringResource(id = emptyHint)) }
    )
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
            style = MaterialTheme.typography.titleMedium
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