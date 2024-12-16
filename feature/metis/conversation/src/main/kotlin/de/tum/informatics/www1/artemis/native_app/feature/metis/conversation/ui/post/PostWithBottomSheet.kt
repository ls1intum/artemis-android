package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions.PostActions
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions.PostContextBottomSheet
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IBasePost

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
    displayHeader: Boolean,
    joinedItemType: PostItemViewJoinedType,
    onClick: () -> Unit
) {
    var displayBottomSheet by remember(post, postItemViewType) { mutableStateOf(false) }

    var color = MaterialTheme.colorScheme.surface

    val cardShape: Shape = when (joinedItemType) {
        PostItemViewJoinedType.HEADER -> MaterialTheme.shapes.small.copy(
            bottomStart = CornerSize(0.dp),
            bottomEnd = CornerSize(0.dp)
        )
        PostItemViewJoinedType.FOOTER -> MaterialTheme.shapes.small.copy(
            topStart = CornerSize(0.dp),
            topEnd = CornerSize(0.dp)
        )
        PostItemViewJoinedType.JOINED, PostItemViewJoinedType.PARENT -> MaterialTheme.shapes.small.copy(all = CornerSize(0.dp))
        PostItemViewJoinedType.SINGLE -> MaterialTheme.shapes.small
    }

    val dynamicModifier = modifier
        .let {
            when (joinedItemType) {
                PostItemViewJoinedType.HEADER -> it.padding(top = 4.dp, bottom = 0.dp)
                PostItemViewJoinedType.FOOTER -> it.padding(top = 0.dp, bottom = 4.dp)
                PostItemViewJoinedType.SINGLE, PostItemViewJoinedType.PARENT -> it.padding(vertical = 4.dp)
                PostItemViewJoinedType.JOINED, -> it.padding(vertical = 0.dp)
            }
        }
        .background(color)

    Card(
        modifier = dynamicModifier,
        shape = cardShape
    ){
        PostItem(
            modifier = modifier,
            post = post,
            postItemViewType = postItemViewType,
            clientId = clientId,
            displayHeader = displayHeader,
            onClickOnReaction = postActions.onClickReaction,
            onClick = onClick,
            onLongClick = {
                displayBottomSheet = true
            },
            onRequestRetrySend = postActions.onRequestRetrySend
        )
    }

    if (displayBottomSheet && post != null) {
        PostContextBottomSheet(
            postActions = postActions,
            post = post,
            clientId = clientId,
            onDismissRequest = {
                displayBottomSheet = false
            }
        )
    }
}