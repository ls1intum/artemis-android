package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.content.Emoji

@Composable
fun EmojiLineItem(
    modifier: Modifier = Modifier,
    emoji: Emoji
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = emoji.unicode,
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = ":${emoji.emojiId}:",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}