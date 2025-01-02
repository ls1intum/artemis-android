package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.saved_posts.SavedPostsViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.StandalonePostId
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.ISavedPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.SavedPostStatus
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.getIcon
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.getUiText

@Composable
internal fun SavedPostsScreen (
    modifier: Modifier,
    viewModel: SavedPostsViewModel,
    onNavigateBack: (() -> Unit)?,
    onNavigateToPost: (postId: StandalonePostId) -> Unit
) {
}

@Composable
internal fun SavedPostsScreen (
    modifier: Modifier,
    status: SavedPostStatus,
    savedPosts: List<ISavedPost>,
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
            modifier = Modifier.padding(paddingValues)
        ) {
            if (status == SavedPostStatus.ARCHIVED || status == SavedPostStatus.COMPLETED) {
                RemovalNotice(
                    modifier = Modifier.padding(paddingValues)
                )
            }

            // TODO: empty state
            LazyColumn(
                modifier = modifier
                    .padding(paddingValues)
            ) {

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
        horizontalArrangement = Arrangement.spacedBy(4.dp)
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