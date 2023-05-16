package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.feature.metis.R

@Composable
internal fun ConversationSettingsBody(
    modifier: Modifier,
    viewModel: ConversationSettingsViewModel
) {
    val conversationDataState by viewModel.conversation.collectAsState()

    val name by viewModel.name.collectAsState()
    val description by viewModel.description.collectAsState()
    val topic by viewModel.topic.collectAsState()

    BasicDataStateUi(
        modifier = modifier,
        dataState = conversationDataState,
        loadingText = stringResource(id = R.string.conversation_settings_loading),
        failureText = stringResource(id = R.string.conversation_settings_failure),
        retryButtonText = stringResource(id = R.string.conversation_settings_try_again),
        onClickRetry = viewModel::requestReload
    ) { conversation ->
        Column(modifier = Modifier.fillMaxSize()) {
            ConversationInfoSettings(
                modifier = Modifier.fillMaxWidth(),
                conversation = conversation,
                name = name,
                description = description,
                topic = topic,
                updateName = viewModel::updateName,
                updateDescription = viewModel::updateDescription,
                updateTopic = viewModel::updateTopic
            )
        }
    }
}