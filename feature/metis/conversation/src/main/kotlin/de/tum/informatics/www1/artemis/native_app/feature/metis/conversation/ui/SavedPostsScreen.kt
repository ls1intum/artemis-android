package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.common.EmptyListHint
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.saved_posts.SavedPostsViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.StandalonePostId
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.ISavedPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.SavedPostStatus
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.getIcon
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.getUiText
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf


@Composable
fun SavedPostsScreen(
    modifier: Modifier = Modifier,
    courseId: Long,
    savedPostStatus: SavedPostStatus,
    onNavigateBack: (() -> Unit)?,
    onNavigateToPost: (postId: StandalonePostId) -> Unit
) {
    val viewModel = koinViewModel<SavedPostsViewModel>(
        key = "$courseId|$savedPostStatus"
    ) { parametersOf(courseId, savedPostStatus) }

    SavedPostsScreen(
        modifier = modifier,
        viewModel = viewModel,
        onNavigateBack = onNavigateBack,
        onNavigateToPost = onNavigateToPost
    )
}


@Composable
internal fun SavedPostsScreen (
    modifier: Modifier,
    viewModel: SavedPostsViewModel,
    onNavigateBack: (() -> Unit)?,
    onNavigateToPost: (postId: StandalonePostId) -> Unit
) {
    val savedPosts by viewModel.savedPosts.collectAsState()
    val status = viewModel.savedPostStatus

    SavedPostsScreen(
        modifier = modifier,
        status = status,
        savedPostsDataState = savedPosts,
        onRequestReload = viewModel::requestReload,
        onNavigateBack = onNavigateBack ?: {},
        onNavigateToPost = onNavigateToPost
    )
}

@Composable
internal fun SavedPostsScreen (
    modifier: Modifier,
    status: SavedPostStatus,
    savedPostsDataState: DataState<List<ISavedPost>>,
    onRequestReload: () -> Unit,
    onNavigateBack: (() -> Unit),
    onNavigateToPost: (postId: StandalonePostId) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    TopBarTitle(status = status)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier.padding(paddingValues)
        ) {
            if (status == SavedPostStatus.ARCHIVED || status == SavedPostStatus.COMPLETED) {
                RemovalNotice()
            }

            BasicDataStateUi(
                modifier = Modifier.fillMaxSize(),
                dataState = savedPostsDataState,
                loadingText = stringResource(R.string.saved_posts_loading_posts_loading),
                failureText = stringResource(R.string.saved_posts_loading_posts_failed),
                retryButtonText = stringResource(R.string.saved_posts_loading_posts_try_again),
                onClickRetry = onRequestReload
            ) { savedPosts ->

                if (savedPosts.isEmpty()) {
                    EmptyListHint(
                        modifier = Modifier.padding(paddingValues),
                        hint = stringResource(
                            id = R.string.saved_posts_empty_state_title,
                            status.getUiText()
                        ),
                        icon = status.getIcon()
                    )
                }

                LazyColumn() {

                }
            }
        }
    }
}


@Composable
private fun TopBarTitle(
    modifier: Modifier = Modifier,
    status: SavedPostStatus
) {
    val textPrefix = stringResource(R.string.saved_posts_title_prefix)
    val text = "$textPrefix (${status.getUiText()})"

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = status.getIcon(),
            contentDescription = null,
        )

        Text(
            text = text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RemovalNotice(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
        )
        Text(
            modifier = modifier,
            text = stringResource(R.string.saved_posts_removal_notice),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}