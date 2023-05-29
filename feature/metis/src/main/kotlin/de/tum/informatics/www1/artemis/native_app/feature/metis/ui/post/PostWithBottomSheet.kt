package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.IBasePost

/**
 * Wrapps [PostItem] and can display the bottom sheet for the post
 */
@Composable
internal fun PostWithBottomSheet(
    modifier: Modifier,
    post: IBasePost?,
    postItemViewType: PostItemViewType,
    postActions: PostActions,
    clientId: Long,
    onClick: () -> Unit
) {
    var displayBottomSheet by remember(post, postItemViewType) { mutableStateOf(false) }

    PostItem(
        modifier = modifier,
        post = post,
        postItemViewType = postItemViewType,
        clientId = clientId,
        onRequestReactWithEmoji = { displayBottomSheet = true },
        onClickOnPresentReaction = postActions.onRequestReactWithEmoji,
        onClick = onClick,
        onLongClick = {
            displayBottomSheet = true
        }
    )

    if (displayBottomSheet) {
        PostContextBottomSheet(
            postActions = postActions,
            onDismissRequest = {
                displayBottomSheet = false
            }
        )
    }
}