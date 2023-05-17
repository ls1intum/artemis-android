package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.core.ui.AwaitDeferredCompletion
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.alert.TextAlertDialog
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.feature.metis.R
import kotlinx.coroutines.Deferred

@Composable
internal fun ConversationSettingsBody(
    modifier: Modifier,
    viewModel: ConversationSettingsViewModel
) {
    val conversationDataState by viewModel.conversation.collectAsState()

    val name by viewModel.name.collectAsState()
    val description by viewModel.description.collectAsState()
    val topic by viewModel.topic.collectAsState()

    val canEdit by viewModel.canEdit.collectAsState()
    val isDirty by viewModel.isDirty.collectAsState()

    var savingJob: Deferred<Boolean>? by remember { mutableStateOf(null) }

    var displaySaveFailedDialog by remember { mutableStateOf(false) }

    AwaitDeferredCompletion(
        job = savingJob,
        onComplete = { successful ->
            savingJob = null

            if (!successful) {
                displaySaveFailedDialog = true
            }
        }
    )

    val editableConversationInfo = remember(name, description, topic, canEdit, isDirty, savingJob) {
        EditableConversationInfo(
            name = name,
            description = description,
            topic = topic,
            updateName = viewModel::updateName,
            updateDescription = viewModel::updateDescription,
            updateTopic = viewModel::updateTopic,
            canEditName = canEdit,
            canEditDescription = canEdit,
            canEditTopic = canEdit,
            isDirty = isDirty,
            isSavingChanges = savingJob != null,
            onRequestSaveChanges = {
                if (savingJob == null) {
                    savingJob = viewModel.saveChanges()
                }
            }
        )
    }

    BasicDataStateUi(
        modifier = modifier,
        dataState = conversationDataState,
        loadingText = stringResource(id = R.string.conversation_settings_loading),
        failureText = stringResource(id = R.string.conversation_settings_failure),
        retryButtonText = stringResource(id = R.string.conversation_settings_try_again),
        onClickRetry = viewModel::requestReload
    ) { conversation ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacings.ScreenHorizontalSpacing)
        ) {
            ConversationInfoSettings(
                modifier = Modifier.fillMaxWidth(),
                conversation = conversation,
                editableConversationInfo = editableConversationInfo
            )
        }
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
