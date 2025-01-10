package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.data.join
import de.tum.informatics.www1.artemis.native_app.core.ui.AwaitDeferredCompletion
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.alert.TextAlertDialog
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.PerformActionOnUserData
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.PerformActionOnUserDialogs
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.UserAction
import kotlinx.coroutines.Deferred

internal val ConversationSettingsSectionTextStyle: TextStyle
    @Composable get() = MaterialTheme.typography.titleLarge

@Composable
internal fun ConversationSettingsBody(
    modifier: Modifier,
    viewModel: ConversationSettingsViewModel,
    onRequestAddMembers: () -> Unit,
    onRequestViewAllMembers: () -> Unit,
    onConversationLeft: () -> Unit
) {
    val conversationDataState by viewModel.conversation.collectAsState()
    val usernameDataState by viewModel.clientUsername.collectAsState()

    val membersDataState by viewModel.previewMembers.collectAsState()

    var userActionData: PerformActionOnUserData? by remember { mutableStateOf(null) }

    val name by viewModel.name.collectAsState()
    val description by viewModel.description.collectAsState()
    val topic by viewModel.topic.collectAsState()

    val isNameIllegal by viewModel.isNameIllegal.collectAsState()
    val isDescriptionIllegal by viewModel.isDescriptionIllegal.collectAsState()
    val isTopicIllegal by viewModel.isTopicIllegal.collectAsState()

    val canEdit by viewModel.canEdit.collectAsState()
    val canSave by viewModel.canSave.collectAsState()
    val isDirty by viewModel.isDirty.collectAsState()

    var savingJob: Deferred<Boolean>? by remember { mutableStateOf(null) }
    var leaveConversationJob: Deferred<Boolean>? by remember { mutableStateOf(null) }
    var archiveChannelJob: Deferred<Boolean>? by remember { mutableStateOf(null) }

    var displaySaveFailedDialog by remember { mutableStateOf(false) }

    val onSaveResult = { successful: Boolean ->
        if (!successful) {
            displaySaveFailedDialog = true
        }
    }

    AwaitDeferredCompletion(
        job = savingJob,
        onComplete = { successful ->
            savingJob = null

            onSaveResult(successful)
        }
    )

    AwaitDeferredCompletion(
        job = leaveConversationJob,
        onComplete = { successful ->
            leaveConversationJob = null

            onSaveResult(successful)

            if (successful) {
                onConversationLeft()
            }
        }
    )

    AwaitDeferredCompletion(
        job = archiveChannelJob,
        onComplete = { successful ->
            archiveChannelJob = null

            onSaveResult(successful)
        }
    )

    val editableConversationInfo = rememberEditableConversationInfo(
        name = name,
        description = description,
        topic = topic,
        isNameIllegal = isNameIllegal,
        isDescriptionIllegal = isDescriptionIllegal,
        isTopicIllegal = isTopicIllegal,
        canEdit = canEdit,
        isDirty = isDirty,
        savingJob = savingJob,
        viewModel = viewModel,
        canSave = canSave,
        onRequestSaveChanges = {
            if (savingJob == null) {
                savingJob = viewModel.saveChanges()
            }
        }
    )

    BasicDataStateUi(
        modifier = modifier,
        dataState = conversationDataState.join(membersDataState, usernameDataState),
        loadingText = stringResource(id = R.string.conversation_settings_loading),
        failureText = stringResource(id = R.string.conversation_settings_failure),
        retryButtonText = stringResource(id = R.string.conversation_settings_try_again),
        onClickRetry = viewModel::requestReload
    ) { (conversation, members, clientUsername) ->
        val conversationSectionModifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacings.ScreenHorizontalSpacing)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ConversationInfoSettings(
                modifier = conversationSectionModifier,
                conversation = conversation,
                editableConversationInfo = editableConversationInfo
            )

            ConversationMemberSettings(
                modifier = Modifier.fillMaxWidth(),
                conversation = conversation,
                clientUsername = clientUsername,
                memberCount = conversation.numberOfMembers,
                members = members,
                hasMoreMembers = conversation.numberOfMembers > 10,
                onRequestAddMembers = onRequestAddMembers,
                onRequestViewAllMembers = onRequestViewAllMembers,
                onRequestKickMember = {
                    userActionData = PerformActionOnUserData(it, UserAction.KICK)
                },
                onRequestGiveModerationRights = {
                    userActionData =
                        PerformActionOnUserData(it, UserAction.GIVE_MODERATION_RIGHTS)
                },
                onRequestRevokeModerationRights = {
                    userActionData =
                        PerformActionOnUserData(it, UserAction.REVOKE_MODERATION_RIGHTS)
                }
            )

            SectionMoreInfo(
                modifier = conversationSectionModifier,
                conversation = conversation
            )

            ConversationOtherSettings(
                modifier = conversationSectionModifier,
                conversation = conversation,
                onLeaveConversation = {
                    leaveConversationJob = viewModel.leaveConversation()
                },
                onToggleChannelArchivation = {
                    archiveChannelJob = viewModel.toggleChannelArchivation()
                }
            )
        }

        PerformActionOnUserDialogs(
            conversation = conversation,
            performActionOnUserData = userActionData,
            viewModel = viewModel,
            onDismiss = { userActionData = null }
        )
    }

    if (displaySaveFailedDialog) {
        TextAlertDialog(
            title = stringResource(id = R.string.conversation_settings_basic_data_save_failed_dialog_title),
            text = stringResource(id = R.string.conversation_settings_basic_data_save_failed_dialog_message),
            confirmButtonText = stringResource(id = R.string.conversation_settings_basic_data_save_failed_dialog_positive),
            dismissButtonText = null,
            onPressPositiveButton = { displaySaveFailedDialog = false },
            onDismissRequest = { displaySaveFailedDialog = false }
        )
    }
}

@Composable
private fun rememberEditableConversationInfo(
    name: String,
    description: String,
    topic: String,
    isNameIllegal: Boolean,
    isDescriptionIllegal: Boolean,
    isTopicIllegal: Boolean,
    canEdit: Boolean,
    isDirty: Boolean,
    savingJob: Deferred<Boolean>?,
    viewModel: ConversationSettingsViewModel,
    canSave: Boolean,
    onRequestSaveChanges: () -> Unit
): EditableConversationInfo {
    return remember(
        name,
        description,
        topic,
        isNameIllegal,
        isDescriptionIllegal,
        isTopicIllegal,
        canEdit,
        isDirty,
        savingJob,
        onRequestSaveChanges
    ) {
        EditableConversationInfo(
            name = name,
            description = description,
            topic = topic,
            isNameIllegal = isNameIllegal,
            isDescriptionIllegal = isDescriptionIllegal,
            isTopicIllegal = isTopicIllegal,
            updateName = viewModel::updateName,
            updateDescription = viewModel::updateDescription,
            updateTopic = viewModel::updateTopic,
            canEditName = canEdit,
            canEditDescription = canEdit,
            canEditTopic = canEdit,
            canSave = canSave,
            isDirty = isDirty,
            isSavingChanges = savingJob != null,
            onRequestSaveChanges = onRequestSaveChanges
        )
    }
}
