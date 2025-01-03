package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.NavigationBackButton
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.MetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.saved_posts.SavedPostWithActions
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.saved_posts.SavedPostsViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.ISavedPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.SavedPostStatus
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.getIcon
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.getUiText
import kotlinx.coroutines.Deferred
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf


@Composable
fun SavedPostsScreen(
    modifier: Modifier = Modifier,
    courseId: Long,
    savedPostStatus: SavedPostStatus,
    onNavigateBack: (() -> Unit),
    onNavigateToPost: (ISavedPost) -> Unit
) {
    val viewModel = koinViewModel<SavedPostsViewModel>(
        key = "$courseId|$savedPostStatus"
    ) { parametersOf(courseId, savedPostStatus) }

    LaunchedEffect(viewModel) {
        viewModel.requestReload()
    }

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
    onNavigateBack: (() -> Unit),
    onNavigateToPost: (ISavedPost) -> Unit
) {
    val savedPosts by viewModel.savedPosts.collectAsState()
    val status = viewModel.savedPostStatus

    SavedPostsScreen(
        modifier = modifier,
        status = status,
        savedPostsDataState = savedPosts,
        onRequestReload = viewModel::requestReload,
        onNavigateBack = onNavigateBack,
        onNavigateToPost = onNavigateToPost,
        onChangeStatus = viewModel::changeSavedPostStatus,
        onRemoveFromSavedPosts = viewModel::removeFromSavedPosts
    )
}

@Composable
internal fun SavedPostsScreen (
    modifier: Modifier,
    status: SavedPostStatus,
    savedPostsDataState: DataState<List<ISavedPost>>,
    onRequestReload: () -> Unit,
    onNavigateBack: (() -> Unit),
    onNavigateToPost: (ISavedPost) -> Unit,
    onChangeStatus: (ISavedPost, SavedPostStatus) -> Deferred<MetisModificationFailure?>,
    onRemoveFromSavedPosts: (ISavedPost) -> Deferred<MetisModificationFailure?>
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { TopBarTitle(status = status) },
                navigationIcon = { NavigationBackButton(onNavigateBack) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier.padding(paddingValues)
        ) {
            if (status == SavedPostStatus.ARCHIVED || status == SavedPostStatus.COMPLETED) {
                RemovalNotice(
                    Modifier.padding(bottom = 8.dp)
                )
            }

            BasicDataStateUi(
                modifier = Modifier.fillMaxSize(),
                dataState = savedPostsDataState,
                loadingText = stringResource(R.string.saved_posts_loading_posts_loading),
                failureText = stringResource(R.string.saved_posts_loading_posts_failed),
                retryButtonText = stringResource(R.string.saved_posts_loading_posts_try_again),
                onClickRetry = onRequestReload
            ) { savedPosts ->

                SavedPostsList(
                    modifier = Modifier.fillMaxSize(),
                    status = status,
                    savedPosts = savedPosts,
                    onNavigateToPost = onNavigateToPost,
                    onChangeStatus = onChangeStatus,
                    onRemoveFromSavedPosts = onRemoveFromSavedPosts
                )
            }
        }
    }
}

@Composable
private fun SavedPostsList(
    modifier: Modifier = Modifier,
    status: SavedPostStatus,
    savedPosts: List<ISavedPost>,
    onNavigateToPost: (ISavedPost) -> Unit,
    onChangeStatus: (ISavedPost, SavedPostStatus) -> Deferred<MetisModificationFailure?>,
    onRemoveFromSavedPosts: (ISavedPost) -> Deferred<MetisModificationFailure?>
) {
    AnimatedContent(
        targetState = savedPosts,
        label = "Animated saved posts list: empty <-> not empty"
    ) { targetSavedPosts ->
        if (targetSavedPosts.isEmpty()) {
            EmptyListHint(
                modifier = modifier,
                hint = stringResource(
                    id = R.string.saved_posts_empty_state_title,
                    status.getUiText()
                ),
                icon = status.getIcon()
            )
            return@AnimatedContent
        }

        LazyColumn(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = targetSavedPosts,
                key = { "${it.serverPostId}|${it.postingType}" }
            ) {
                SavedPostWithActions(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem(),
                    savedPost = it,
                    onClick = {
                        onNavigateToPost(it)
                    },
                    onChangeStatus = { newStatus ->
                        onChangeStatus(it, newStatus)
                    },
                    onRemoveFromSavedPosts = {
                        onRemoveFromSavedPosts(it)
                    }
                )
            }

            item {
                // Add a spacer at the end to indicate the end of the list
                Spacer(Modifier.height(32.dp))
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
            text = stringResource(R.string.saved_posts_removal_notice),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}
