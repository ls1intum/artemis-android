package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.user_conversation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.feature.metis.NavigateToUserConversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.R
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun NavigateToUserConversationUi(
    modifier: Modifier,
    courseId: Long,
    navigation: NavigateToUserConversation,
    onNavigateToConversation: (Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    val viewModel: NavigateToUserConversationViewModel =
        koinViewModel { parametersOf(courseId, navigation.userIdentifier) }

    val conversationIdDataState by viewModel.conversationId.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { }, navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                }
            })
        }
    ) { padding ->
        BasicDataStateUi(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            dataState = conversationIdDataState,
            loadingText = stringResource(id = R.string.navigate_to_user_conversation_ui_loading),
            failureText = stringResource(id = R.string.navigate_to_user_conversation_ui_failed),
            retryButtonText = stringResource(id = R.string.navigate_to_user_conversation_ui_try_again),
            onClickRetry = viewModel::onRequestReload
        ) { conversationId ->
            LaunchedEffect(key1 = conversationId) {
                if (conversationId != null) {
                    onNavigateToConversation(conversationId)
                } else {
                    // Username is the client itself. Fallback to overview.
                    onNavigateBack()
                }
            }
        }
    }
}