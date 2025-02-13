package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.members

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.common.top_app_bar.ArtemisSearchTopAppBar
import de.tum.informatics.www1.artemis.native_app.core.ui.common.top_app_bar.CollapsingContentState
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.NavigationBackButton
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.R
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ConversationMembersScreen(
    modifier: Modifier,
    courseId: Long,
    conversationId: Long,
    onNavigateBack: () -> Unit
) {
    val viewModel: ConversationMembersViewModel = koinViewModel {
        parametersOf(
            courseId,
            conversationId
        )
    }

    val query by viewModel.query.collectAsState()

    val collapsingContentState = CollapsingContentState(
        initialCollapsingHeight = 0f,
        initialOffset = 0f
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            ArtemisSearchTopAppBar(
                navigationIcon = { NavigationBackButton(onNavigateBack) },
                title = {
                    Text(text = stringResource(id = R.string.conversation_members_title))
                },
                query = query,
                searchBarHint = stringResource(id = R.string.conversation_members_query_placeholder),
                updateQuery = viewModel::updateQuery,
                collapsingContentState = collapsingContentState
            )
        }
    ) { padding ->
        // Animate the padding to provide smooth search transitions
        val animatedPadding by animateDpAsState(
            targetValue = padding.calculateTopPadding(),
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
        )

        ConversationMembersBody(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = animatedPadding)
                .consumeWindowInsets(WindowInsets.systemBars.only(WindowInsetsSides.Top))
                .padding(horizontal = Spacings.ScreenHorizontalSpacing),
            courseId = courseId,
            collapsingContentState = collapsingContentState,
            conversationId = conversationId
        )
    }
}
