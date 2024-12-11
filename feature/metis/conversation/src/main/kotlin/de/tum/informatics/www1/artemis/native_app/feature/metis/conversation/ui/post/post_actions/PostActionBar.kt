package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
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
            color = MaterialTheme.colorScheme.onSurface,
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
        IconButton(
            onClick = {
                postActions.onCopyText()
            }
        ) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = null
            )
        }

        postActions.onPinPost?.let {
            VerticalDivider()

            IconButton(
                modifier = Modifier.testTag(TEST_TAG_PIN_POST),
                onClick = {
                    postActions.onPinPost.invoke()
                }
            ) {
                if (post.displayPriority == DisplayPriority.PINNED) Icon(
                    ImageVector.vectorResource(
                        R.drawable.unpin
                    ), null, tint = MaterialTheme.colorScheme.primary
                ) else Icon(ImageVector.vectorResource(R.drawable.pin), null, tint = MaterialTheme.colorScheme.primary)
            }
        }

        if (postActions.requestEditPost != null || postActions.requestDeletePost != null) {
            VerticalDivider()
        }

        postActions.requestEditPost?.let {
            IconButton(
                modifier = Modifier.testTag(TEST_TAG_POST_EDIT),
                onClick = {
                    it()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = null,
                )
            }
        }

        postActions.requestDeletePost?.let {
            IconButton(
                modifier = Modifier.testTag(TEST_TAG_POST_DELETE),
                onClick = {
                    it()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    tint = PostColors.Actions.delete,
                    contentDescription = null
                )
            }
        }
    }
}