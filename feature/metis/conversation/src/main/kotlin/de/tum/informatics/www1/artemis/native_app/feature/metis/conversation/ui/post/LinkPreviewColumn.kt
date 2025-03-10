package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.model.LinkPreview

@Composable
fun LinkPreviewColumn(
    modifier: Modifier,
    linkPreviews: List<LinkPreview>,
    isAuthor: Boolean,
    onRemoveLinkPreview: (LinkPreview) -> Unit
) {
    Column(modifier = modifier) {
        val shownImagesCount = linkPreviews.filter { it.shouldPreviewBeShown }.size
        val showImage = remember { shownImagesCount <= 1 }

        linkPreviews.forEach { linkPreview ->
            if (linkPreview.shouldPreviewBeShown) {
                LinkPreviewItem(
                    modifier = Modifier.fillMaxWidth(),
                    linkPreview = linkPreview,
                    isAuthor = isAuthor,
                    showImage = showImage,
                    onRemoveLinkPreview = onRemoveLinkPreview
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun LinkPreviewItem(
    modifier: Modifier,
    linkPreview: LinkPreview,
    isAuthor: Boolean,
    showImage: Boolean,
    onRemoveLinkPreview: (LinkPreview) -> Unit
) {
    val context = LocalContext.current

    QuotedMessageContainer(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacings.Post.innerSpacing)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacings.Post.innerSpacing)
            ){
                Text(
                    modifier = Modifier.weight(1f),
                    text = linkPreview.title,
                    style = MaterialTheme.typography.titleMedium
                )

                if (isAuthor) {
                    Spacer(modifier = Modifier.width(Spacings.Post.innerSpacing))

                    IconButton(
                        modifier = Modifier.size(24.dp),
                        onClick = {
                            linkPreview.shouldPreviewBeShown = false
                            onRemoveLinkPreview(linkPreview)
                        }
                    ) {
                        Icon(
                            modifier = Modifier.size(18.dp),
                            imageVector = Icons.Default.Close,
                            contentDescription = null
                        )
                    }
                }
            }

            Text(
                text = linkPreview.description,
                style = MaterialTheme.typography.bodyMedium
            )

            if (showImage){
                val model = remember(linkPreview.image) {
                    ImageRequest
                        .Builder(context)
                        .data(linkPreview.image)
                        .build()
                }

                AsyncImage(
                    modifier = Modifier.size(94.dp),
                    model = model,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
fun QuotedMessageContainer(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(Spacings.Post.innerSpacing)
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .fillMaxHeight()
                .width(Spacings.Post.quoteBorderWidth)
                .background(MaterialTheme.colorScheme.primary)
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacings.Post.innerSpacing),
            content = content
        )
    }
}
