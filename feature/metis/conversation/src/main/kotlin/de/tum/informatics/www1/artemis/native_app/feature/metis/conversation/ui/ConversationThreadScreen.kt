package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.LocalReplyAutoCompleteHintProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.shared.MetisOutdatedBanner
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.thread.MetisThreadUi
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.thread.ConversationThreadUseCase
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.thread.StandalonePostId
import kotlinx.serialization.json.Json
import org.koin.androidx.compose.getViewModel
import org.koin.core.parameter.parametersOf

/**
 * Display the post and its replied. If metis may be outdated, a banner will be displayed to the user.
 */
@Composable
internal fun ConversationThreadScreen(
    modifier: Modifier,
    viewModel: ConversationViewModel,
    standalonePostId: StandalonePostId,
    metisContext: MetisContext,
    onNavigateUp: () -> Unit
) {
    val isDataOutdated by viewModel.isDataOutdated.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.standalone_screen_title))
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::requestReload) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            MetisOutdatedBanner(
                modifier = Modifier.fillMaxWidth(),
                isOutdated = isDataOutdated,
                requestRefresh = viewModel::requestReload
            )

            CompositionLocalProvider(LocalReplyAutoCompleteHintProvider provides viewModel) {
                MetisThreadUi(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    viewModel = viewModel
                )
            }
        }
    }
}
