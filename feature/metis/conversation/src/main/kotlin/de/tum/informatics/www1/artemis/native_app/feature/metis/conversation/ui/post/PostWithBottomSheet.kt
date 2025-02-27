package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.material.colors.PostColors
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions.EmojiSelection
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions.PostActions
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions.PostContextBottomSheet
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions.PostReactionBottomSheet
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.DisplayPriority
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IAnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IBasePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IStandalonePost

/**
 * Wrapps [PostItem] and can display the bottom sheet for the post
 */
@Composable
internal fun PostWithBottomSheet(
    modifier: Modifier,
    post: IBasePost?,
    postItemViewType: PostItemViewType,
    isMarkedAsDeleteList: SnapshotStateList<IBasePost>,
    postActions: PostActions,
    clientId: Long,
    displayHeader: Boolean,
    joinedItemType: PostItemViewJoinedType,
    onClick: () -> Unit
) {
    var displayBottomSheet by remember(post, postItemViewType) { mutableStateOf(false) }
    var displayReactionBottomSheet by remember(post, postItemViewType) { mutableStateOf(false) }
    var emojiSelection: EmojiSelection by remember { mutableStateOf(EmojiSelection.ALL) }

    val isPinned = post is IStandalonePost && post.displayPriority == DisplayPriority.PINNED
    val isResolving = post is IAnswerPost && post.resolvesPost
    val isParentPostInThread = joinedItemType == PostItemViewJoinedType.PARENT
    val isSaved = post?.isSaved == true

    val cardColor = when {
        isParentPostInThread -> MaterialTheme.colorScheme.background
        isResolving -> PostColors.StatusBackground.resolving
        isPinned -> PostColors.StatusBackground.pinned
        isSaved -> PostColors.StatusBackground.saved
        else -> CardDefaults.cardColors().containerColor
    }

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

    val applyPaddingToModifier: @Composable (modifier: Modifier, paddingValue: Dp) -> Modifier =
        { mod, paddingValue ->
            when (joinedItemType) {
                PostItemViewJoinedType.HEADER -> mod.padding(top = paddingValue, bottom = 0.dp)
                PostItemViewJoinedType.FOOTER -> mod.padding(top = 0.dp, bottom = paddingValue)
                PostItemViewJoinedType.SINGLE -> mod.padding(
                    vertical = paddingValue
                )
                PostItemViewJoinedType.JOINED -> mod.padding(vertical = 0.dp)
                PostItemViewJoinedType.PARENT -> mod.padding(0.dp)
            }
        }

    val cardModifier = applyPaddingToModifier(modifier, 4.dp)
    val innerModifier = applyPaddingToModifier(Modifier, Spacings.Post.innerSpacing)

    Card(
        modifier = cardModifier,
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ){
        PostItem(
            modifier = innerModifier,
            post = post,
            postItemViewType = postItemViewType,
            clientId = clientId,
            displayHeader = displayHeader,
            postItemViewJoinedType = joinedItemType,
            isMarkedAsDeleteList = isMarkedAsDeleteList,
            postActions = postActions,
            onClick = onClick,
            onLongClick = {
                displayBottomSheet = true
            },
            onRequestRetrySend = postActions.onRequestRetrySend,
            onShowReactionsBottomSheet = {
                emojiSelection = it
                displayReactionBottomSheet = true
            }
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

    if (displayReactionBottomSheet && post != null) {
        PostReactionBottomSheet(
            post = post,
            emojiSelection = emojiSelection,
            onDismissRequest = {
                displayReactionBottomSheet = false
            }
        )
    }
}