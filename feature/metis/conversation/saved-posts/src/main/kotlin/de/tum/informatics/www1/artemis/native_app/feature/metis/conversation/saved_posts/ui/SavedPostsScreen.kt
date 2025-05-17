package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.saved_posts.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.common.EmptyListHint
import de.tum.informatics.www1.artemis.native_app.core.ui.common.InfoMessageCard
import de.tum.informatics.www1.artemis.native_app.core.ui.common.top_app_bar.AdaptiveNavigationIcon
import de.tum.informatics.www1.artemis.native_app.core.ui.common.top_app_bar.ArtemisTopAppBar
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.NavigationBackButton
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.LocalMarkdownTransformer
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.ProvideMarkwon
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.rememberPostArtemisMarkdownTransformer
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.saved_posts.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.service.MetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.ui.ChatListItem
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.ISavedPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.SavedPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.SavedPostStatus
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.getIcon
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.getTintColor
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.getUiText
import kotlinx.coroutines.Deferred
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun SavedPostsScreen(
    modifier: Modifier = Modifier,
    courseId: Long,
    onNavigateToPost: (ISavedPost) -> Unit,
    onSidebarToggle: () -> Unit
) {
    val viewModel = koinViewModel<SavedPostsViewModel>(
        key = "$courseId"
    ) { parametersOf(courseId) }

    LaunchedEffect(viewModel) {
        viewModel.onRequestReload()
    }

    SavedPostsScreen(
        modifier = modifier,
        viewModel = viewModel,
        onNavigateToPost = onNavigateToPost,
        onSidebarToggle = onSidebarToggle
    )
}


@Composable
internal fun SavedPostsScreen(
    modifier: Modifier,
    viewModel: SavedPostsViewModel,
    onNavigateToPost: (ISavedPost) -> Unit,
    onSidebarToggle: () -> Unit
) {
    val savedPosts by viewModel.savedPosts.collectAsState()

    val courseId = viewModel.courseId
    val serverUrl by viewModel.serverUrl.collectAsState()
    val markdownTransformer = rememberPostArtemisMarkdownTransformer(serverUrl, courseId)

    CompositionLocalProvider(LocalMarkdownTransformer provides markdownTransformer) {
        SavedPostsScreen(
            modifier = modifier,
            savedPostsChatListItemsDataState = savedPosts,
            onRequestReload = viewModel::onRequestReload,
            onNavigateToPost = onNavigateToPost,
            onChangeStatus = viewModel::changeSavedPostStatus,
            onRemoveFromSavedPosts = viewModel::removeFromSavedPosts,
            onSidebarToggle = onSidebarToggle
        )
    }
}

@Composable
internal fun SavedPostsScreen(
    modifier: Modifier,
    savedPostsChatListItemsDataState: DataState<List<ChatListItem.PostItem.SavedItem>>,
    onRequestReload: () -> Unit,
    onNavigateToPost: (ISavedPost) -> Unit,
    onChangeStatus: (ISavedPost, SavedPostStatus) -> Deferred<MetisModificationFailure?>,
    onRemoveFromSavedPosts: (ISavedPost) -> Deferred<MetisModificationFailure?>,
    onSidebarToggle: () -> Unit
) {
    var status by remember { mutableStateOf(SavedPostStatus.IN_PROGRESS) }

    Scaffold(
        modifier = modifier,
        topBar = {
            ArtemisTopAppBar(
                title = { Text(stringResource(R.string.saved_posts_screen_title)) },
                navigationIcon = { AdaptiveNavigationIcon(onSidebarToggle) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(top = paddingValues.calculateTopPadding())
                .consumeWindowInsets(WindowInsets.systemBars.only(WindowInsetsSides.Top))
                .padding(top = Spacings.ScreenTopBarSpacing)
        ) {
            StatusSelectionRow(
                selectedStatus = status,
                onStatusSelected = { status = it }
            )

            BasicDataStateUi(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Spacings.ScreenHorizontalSpacing)
                ,
                dataState = savedPostsChatListItemsDataState,
                loadingText = stringResource(R.string.saved_posts_loading_posts_loading),
                failureText = stringResource(R.string.saved_posts_loading_posts_failed),
                retryButtonText = stringResource(R.string.saved_posts_loading_posts_try_again),
                onClickRetry = onRequestReload,
                enablePullToRefresh = false
            ) { savedPostChatListItems ->
                ProvideMarkwon {
                    SavedPostsList(
                        modifier = Modifier.fillMaxSize(),
                        status = status,
                        savedPostChatListItems = savedPostChatListItems.filter { (it.post as SavedPost).savedPostStatus == status },
                        onNavigateToPost = onNavigateToPost,
                        onChangeStatus = onChangeStatus,
                        onRemoveFromSavedPosts = onRemoveFromSavedPosts
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusSelectionRow(
    selectedStatus: SavedPostStatus,
    onStatusSelected: (SavedPostStatus) -> Unit,
) {
    val filterChipColorAlpha = 0.8f

    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = Spacings.ScreenHorizontalSpacing),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SavedPostStatus.entries.forEach { status ->
            val isSelected = status == selectedStatus
            FilterChip(
                selected = isSelected,
                onClick = {
                    onStatusSelected(status)
                },
                label = {
                    Text(
                        text = status.getUiText(),
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = status.getIcon(),
                        tint = if (isSelected) Color.White else MaterialTheme.colorScheme.primary,
                        contentDescription = null
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = status.getTintColor()
                        .copy(alpha = filterChipColorAlpha)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = MaterialTheme.colorScheme.outlineVariant,
                    selectedBorderColor = MaterialTheme.colorScheme.primary,
                    enabled = true,
                    selected = isSelected,
                )
            )
        }
    }
}


@Composable
private fun SavedPostsList(
    modifier: Modifier = Modifier,
    status: SavedPostStatus,
    savedPostChatListItems: List<ChatListItem.PostItem.SavedItem>,
    onNavigateToPost: (ISavedPost) -> Unit,
    onChangeStatus: (ISavedPost, SavedPostStatus) -> Deferred<MetisModificationFailure?>,
    onRemoveFromSavedPosts: (ISavedPost) -> Deferred<MetisModificationFailure?>
) {
    val removalNotice = @Composable {
        if (status == SavedPostStatus.ARCHIVED || status == SavedPostStatus.COMPLETED) {
            InfoMessageCard(
                modifier = Modifier.padding(vertical = 8.dp),
                infoText = stringResource(R.string.saved_posts_removal_notice)
            )
        }
    }

    AnimatedContent(
        targetState = savedPostChatListItems.isEmpty(),
        label = "Animated saved posts list: empty <-> not empty"
    ) { isEmpty ->
        if (isEmpty) {
            Column(
                modifier = modifier,
            ) {
                removalNotice()

                EmptyListHint(
                    modifier = Modifier.fillMaxSize(),
                    hint = stringResource(
                        id = R.string.saved_posts_empty_state_title,
                        status.getUiText()
                    ),
                    imageVector = status.getIcon()
                )
            }
            return@AnimatedContent
        }

        LazyColumn(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = Spacings.calculateEndOfPagePaddingValues()
        ) {
            item { removalNotice() }

            items(
                items = savedPostChatListItems,
                key = { it.key }
            ) {
                val savedPost = it.post as ISavedPost
                SavedPostWithActions(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem(),
                    savedPostChatListItem = it,
                    onClick = {
                        onNavigateToPost(savedPost)
                    },
                    onChangeStatus = { newStatus ->
                        onChangeStatus(savedPost, newStatus)
                    },
                    onRemoveFromSavedPosts = {
                        onRemoveFromSavedPosts(savedPost)
                    }
                )
            }
        }
    }
}
