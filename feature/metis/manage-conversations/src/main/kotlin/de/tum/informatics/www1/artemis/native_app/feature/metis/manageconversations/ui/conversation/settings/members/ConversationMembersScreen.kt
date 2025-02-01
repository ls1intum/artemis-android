package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.members

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.NavigationBackButton
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.R

@Composable
fun ConversationMembersScreen(
    modifier: Modifier,
    courseId: Long,
    conversationId: Long,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = { NavigationBackButton(onNavigateBack) },
                title = {
                    Text(text = stringResource(id = R.string.conversation_members_title))
                }
            )
        }
    ) { padding ->
        ConversationMembersBody(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .consumeWindowInsets(WindowInsets.systemBars.only(WindowInsetsSides.Top))
                .padding(horizontal = Spacings.ScreenHorizontalSpacing),
            courseId = courseId,
            conversationId = conversationId
        )
    }
}
