package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.ui.material.colors.PostColors
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.DisplayPriority
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IStandalonePost

internal const val TEST_TAG_PIN_POST = "TEST_TAG_PIN_POST"
internal const val TEST_TAG_SAVE_POST = "TEST_TAG_SAVE_POST"
internal const val TEST_TAG_POST_EDIT = "TEST_TAG_POST_EDIT"
internal const val  TEST_TAG_POST_DELETE = "TEST_TAG_POST_DELETE"


@Composable
fun PostActionBar(
    modifier: Modifier,
    post: IStandalonePost,
    postActions: PostActions,
    repliesCount: Int
) {
    HorizontalDivider()

    Row(
        modifier = modifier.height(28.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 8.dp),
            text = pluralStringResource(
                id = R.plurals.post_replies_count,
                count = repliesCount,
                repliesCount
            ),
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.weight(1f))

        ActionBar(post, postActions)
    }

    HorizontalDivider()
}

@Composable
private fun ActionBar(
    post: IStandalonePost,
    postActions: PostActions
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NullableActionIconButton(
            onClick = postActions.onCopyText,
            icon = Icons.Default.ContentCopy,
        )

        PinSaveActionGroup(
            post = post,
            requestPinPost = postActions.onPinPost,
            requestSavePost = postActions.onSavePost
        )

        EditDeleteActionGroup(
            requestEditPost = postActions.requestEditPost,
            requestDeletePost = postActions.requestDeletePost
        )
    }
}

@Composable
private fun PinSaveActionGroup(
    post: IStandalonePost,
    requestPinPost: (() -> Unit)?,
    requestSavePost: (() -> Unit)?
) {
    if (requestPinPost == null && requestSavePost == null) {
        return
    }

    VerticalDivider()

    val pinIconDrawableRes = if (post.displayPriority == DisplayPriority.PINNED) R.drawable.unpin else R.drawable.pin
    NullableActionIconButton(
        modifier = Modifier.testTag(TEST_TAG_PIN_POST),
        onClick = requestPinPost,
        icon = ImageVector.vectorResource(pinIconDrawableRes),
    )

    val saveIcon = if (post.isSaved == true) Icons.Default.BookmarkRemove else Icons.Default.BookmarkAdd
    NullableActionIconButton(
        modifier = Modifier.testTag(TEST_TAG_SAVE_POST),
        onClick = requestSavePost,
        icon = saveIcon,
    )
}

@Composable
private fun EditDeleteActionGroup(
    requestEditPost: (() -> Unit)?,
    requestDeletePost: (() -> Unit)?
) {
    if (requestEditPost == null && requestDeletePost == null) {
        return
    }

    VerticalDivider()

    NullableActionIconButton(
        modifier = Modifier.testTag(TEST_TAG_POST_EDIT),
        onClick = requestEditPost,
        icon = Icons.Default.Edit,
    )

    NullableActionIconButton(
        modifier = Modifier.testTag(TEST_TAG_POST_DELETE),
        onClick = requestDeletePost,
        icon = Icons.Default.Delete,
        tint = PostColors.Actions.delete
    )
}

@Composable
private fun NullableActionIconButton(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)?,
    icon: ImageVector,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    if (onClick == null) {
        return
    }

    IconButton(
        modifier = modifier,
        onClick = onClick
    ) {
        Icon(
            imageVector = icon,
            tint = tint,
            contentDescription = null
        )
    }
}