package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.model.LinkPreview

@Composable
fun LinkPreviewColumn(
    modifier: Modifier,
    linkPreviews: List<LinkPreview>
) {
    Column(modifier = modifier) {
        linkPreviews.forEach { linkPreview ->
            LinkPreviewItem(linkPreview = linkPreview)
        }
    }
}

@Composable
private fun LinkPreviewItem(
    linkPreview: LinkPreview
) {
    Row(
        modifier = Modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.secondaryContainer)
                .width(12.dp)
                .clip(MaterialTheme.shapes.small)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(linkPreview.title)
        }
    }
}