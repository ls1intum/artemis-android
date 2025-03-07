package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.content.Emoji

@Composable
fun EmojiPickerModalBottomSheet(
    modifier: Modifier = Modifier,
    onEmojiClicked: (Emoji) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        modifier = modifier.statusBarsPadding(),
        onDismissRequest = onDismiss,
    ) {
        EmojiPicker(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            onEmojiClicked = onEmojiClicked
        )
    }
}