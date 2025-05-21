package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.overview

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.core.ui.common.top_app_bar.AdaptiveNavigationIcon
import de.tum.informatics.www1.artemis.native_app.core.ui.common.top_app_bar.ArtemisTopAppBar
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.NavigationBackButton
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.R
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ConversationSettingsScreen(
    modifier: Modifier,
    courseId: Long,
    conversationId: Long,
    onNavigateBack: () -> Unit,
    onRequestAddMembers: () -> Unit,
    onRequestViewAllMembers: () -> Unit,
    onConversationLeft: () -> Unit,
    onChannelDeleted: () -> Unit,
    onSidebarToggle: () -> Unit
) {
    ConversationSettingsScreen(
        modifier = modifier,
        viewModel = koinViewModel { parametersOf(courseId, conversationId) },
        courseId = courseId,
        conversationId = conversationId,
        onNavigateBack = onNavigateBack,
        onRequestAddMembers = onRequestAddMembers,
        onRequestViewAllMembers = onRequestViewAllMembers,
        onConversationLeft = onConversationLeft,
        onChannelDeleted = onChannelDeleted,
        onSidebarToggle = onSidebarToggle
    )
}

@Composable
internal fun ConversationSettingsScreen(
    modifier: Modifier,
    viewModel: ConversationSettingsViewModel,
    courseId: Long,
    conversationId: Long,
    onNavigateBack: () -> Unit,
    onRequestAddMembers: () -> Unit,
    onRequestViewAllMembers: () -> Unit,
    onConversationLeft: () -> Unit,
    onChannelDeleted: () -> Unit,
    onSidebarToggle: () -> Unit
) {
    LaunchedEffect(courseId, conversationId) {
        viewModel.updateConversation(courseId, conversationId)
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.onRequestReload()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            ArtemisTopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.conversation_settings_title))
                },
                navigationIcon = {
                    AdaptiveNavigationIcon(onNavigateBack = onNavigateBack, onSidebarToggle = onSidebarToggle)
                }
            )
        }
    ) { paddingValues ->
        ConversationSettingsBody(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(top = paddingValues.calculateTopPadding())
                .consumeWindowInsets(WindowInsets.systemBars.only(WindowInsetsSides.Top)),
            viewModel = viewModel,
            onRequestViewAllMembers = onRequestViewAllMembers,
            onRequestAddMembers = onRequestAddMembers,
            onConversationLeft = onConversationLeft,
            onChannelDeleted = onChannelDeleted
        )
    }
}
