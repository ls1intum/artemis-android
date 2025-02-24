package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.model.LinkPreview

@Composable
fun LinkPreviewColumn(
    modifier: Modifier,
    linkPreviews: List<LinkPreview>
) {
    Column(modifier = modifier) {
        linkPreviews.forEach { linkPreview ->
            if (linkPreview.shouldPreviewBeShown) {
                LinkPreviewItem(
                    modifier = Modifier.fillMaxWidth(),
                    linkPreview = linkPreview
                )
            }
        }
    }
}

@Composable
private fun LinkPreviewItem(
    modifier: Modifier,
    linkPreview: LinkPreview,
) {
    val context = LocalContext.current

    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(shape = MaterialTheme.shapes.medium)
                .fillMaxHeight()
                .width(8.dp)
                .background(color = MaterialTheme.colorScheme.primary)
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val model = remember(linkPreview.image) {
                ImageRequest
                    .Builder(context)
                    .data(linkPreview.image)
                    .build()
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    text = linkPreview.title,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Text(
                text = linkPreview.description,
                style = MaterialTheme.typography.bodyMedium
            )

            AsyncImage(
                modifier = Modifier.size(94.dp),
                model = model,
                contentDescription = null
            )
        }
    }
}