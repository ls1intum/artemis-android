package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IBasePost

@Composable
fun ForwardedMessageColumn(
    modifier: Modifier,
    post: IBasePost?,
    postItemViewType: PostItemViewType,
) {
    val forwardedPosts = when (postItemViewType) {
        is PostItemViewType.ChatListItem.PostWithForwardedMessage -> postItemViewType.forwardedPosts
        is PostItemViewType.ThreadContextItem.PostWithForwardedMessage -> postItemViewType.forwardedPosts
        is PostItemViewType.ThreadAnswerItem.PostWithForwardedMessage -> postItemViewType.forwardedPosts
        else -> emptyList()
    }

    Column(
        modifier = modifier,
    ) {
        forwardedPosts.forEach{ forwardedPost ->
            ForwardedMessageItem(
                modifier = Modifier.fillMaxWidth(),
                forwardedPost = forwardedPost,
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ForwardedMessageItem(
    modifier: Modifier,
    forwardedPost: IBasePost,
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(shape = CircleShape)
                .fillMaxHeight()
                .width(6.dp)
                .background(color = MaterialTheme.colorScheme.primary)
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    modifier = Modifier.height(14.dp),
                    painter = painterResource(id = R.drawable.forward),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )

                Text(
                    text = stringResource(R.string.post_forwarded_from, "DDDD"), // TODO
                    style = MaterialTheme.typography.bodySmall,
                )

                VerticalDivider(modifier = Modifier.height(12.dp), color = MaterialTheme.colorScheme.onSurface)

                Text(
                    modifier = Modifier.clickable {  }, // TODO
                    text = stringResource(R.string.post_forwarded_view_conversation),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            PostItemMainContent(
                modifier = Modifier.wrapContentHeight(unbounded = true),
                post = forwardedPost,
                onClick = {},
                onLongClick = {},
            )
        }
    }
}
