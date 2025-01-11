package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.isSuccess
import de.tum.informatics.www1.artemis.native_app.core.ui.BuildConfig
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.LocalReplyAutoCompleteHintProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.shared.ConversationDataStatusButton
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.thread.MetisThreadUi
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.ConversationIcon
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.humanReadableName

/**
 * Display the post and its replied. If metis may be outdated, a banner will be displayed to the user.
 */
@Composable
internal fun ConversationThreadScreen(
    modifier: Modifier,
    viewModel: ConversationViewModel,
    onNavigateUp: () -> Unit
) {
    viewModel.onCloseThread = onNavigateUp

    val conversation by viewModel.conversation.collectAsState()
    // TODO: introduce rememberDerivedConversationName
    val conversationName by remember(conversation) {
        derivedStateOf {
            conversation.bind { it.humanReadableName }.orElse("Conversation")
        }
    }
    val clientId by viewModel.clientIdOrDefault.collectAsState()

    val dataStatus by viewModel.conversationDataStatus.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(id = R.string.thread_view_title),
                            style = MaterialTheme.typography.titleMedium
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (conversation.isSuccess) {
                                ConversationIcon(
                                    modifier = Modifier.size(16.dp),
                                    conversation = (conversation as DataState.Success).data,
                                    clientId = clientId,
                                )
                            }

                            Text(
                                text = conversationName,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    if (BuildConfig.DEBUG){
                        ConversationDataStatusButton(
                            dataStatus = dataStatus,
                            onRequestSoftReload = viewModel::requestReload
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .navigationBarsPadding(),
        ) {
            CompositionLocalProvider(LocalReplyAutoCompleteHintProvider provides viewModel) {
                MetisThreadUi(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(top = padding.calculateTopPadding())
                        .consumeWindowInsets(WindowInsets.systemBars.only(WindowInsetsSides.Top)),
                    viewModel = viewModel
                )
            }
        }
    }
}
