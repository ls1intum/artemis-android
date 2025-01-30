package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.getUnicodeForEmojiId
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IBasePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IReaction

internal const val TEST_TAG_POST_REACTIONS_BOTTOM_SHEET = "TEST_TAG_POST_REACTIONS_BOTTOM_SHEET"

sealed class EmojiSelection {
    data object ALL : EmojiSelection()
    data class SINGLE(val emojiId: String) : EmojiSelection()
}

@Composable
fun PostReactionBottomSheet(
    post: IBasePost,
    emojiSelection: EmojiSelection,
    onDismissRequest: () -> Unit
) {
    ModalBottomSheet(
        modifier = Modifier
            .statusBarsPadding()
            .testTag(TEST_TAG_POST_REACTIONS_BOTTOM_SHEET),
        contentWindowInsets = { WindowInsets.statusBars },
        sheetState = rememberModalBottomSheetState(),
        onDismissRequest = onDismissRequest
    ) {
        var currentEmojiSelection: EmojiSelection by remember { mutableStateOf(emojiSelection) }
        val reactions = post.reactions ?: emptyList()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 400.dp, max = 600.dp)
                .verticalScroll(rememberScrollState())
                .padding(Spacings.BottomSheetContentPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Chip(
                    text = reactions.size.toString(),
                    currentEmojiSelection = currentEmojiSelection,
                    emojiSelection = EmojiSelection.ALL,
                    onClick = {
                        currentEmojiSelection = EmojiSelection.ALL
                    }
                )

                PostReactionsBar(
                    emojis = reactions,
                    currentEmojiSelection = currentEmojiSelection,
                    onClick = {
                        currentEmojiSelection = it
                    }
                )
            }


        }
    }
}

@Composable
private fun PostReactionsBar(
    emojis: List<IReaction>,
    currentEmojiSelection: EmojiSelection,
    onClick: (EmojiSelection) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(Spacings.Post.innerSpacing),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (emoji in emojis.distinctBy { it.emojiId }) {
            Chip(
                text = emoji.emojiId,
                currentEmojiSelection = currentEmojiSelection,
                emojiSelection = EmojiSelection.SINGLE(emoji.emojiId),
                onClick = onClick
            )
        }
    }
}

@Composable
private fun Chip(
    modifier: Modifier = Modifier,
    text: String,
    currentEmojiSelection: EmojiSelection,
    emojiSelection: EmojiSelection,
    onClick: (EmojiSelection) -> Unit,
) {
    val shape = CircleShape
    val backgroundColor =
        if (currentEmojiSelection == emojiSelection) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.background

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color = backgroundColor, shape)
            .heightIn(max = Spacings.Post.emojiHeight)
            .clickable {
                onClick(emojiSelection)
            }
    ) {
        Text(
            text = if (emojiSelection is EmojiSelection.SINGLE) getUnicodeForEmojiId(emojiId = text) else text,
            fontSize = 12.sp
        )
    }
}