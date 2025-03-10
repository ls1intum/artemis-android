package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.getUnicodeForEmojiId
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IBasePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IReaction
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.PostPojo

internal const val TEST_TAG_POST_REACTIONS_BOTTOM_SHEET = "TEST_TAG_POST_REACTIONS_BOTTOM_SHEET"
fun getTestTagForEmojiId(emojiId: String, source: String) = "emoji$emojiId$source"
fun getTestTagForReactionAuthor(emojiId: String, reactionId: Long, username: String) =
    "reactionAuthor$reactionId$username$emojiId"

sealed class EmojiSelection {
    data object ALL : EmojiSelection()
    data class SINGLE(val emojiId: String) : EmojiSelection()
}

private data class ReactionAuthor(
    val id: Long,
    val username: String,
    val emojiId: String
)

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
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        onDismissRequest = onDismissRequest
    ) {
        var currentEmojiSelection: EmojiSelection by remember { mutableStateOf(emojiSelection) }
        val reactions = post.reactions ?: emptyList()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 400.dp, max = 600.dp)
                .padding(horizontal = Spacings.ScreenHorizontalSpacing)
                .consumeWindowInsets(WindowInsets.statusBars),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Chip(
                    text = stringResource(id = R.string.post_reactions_show_all, reactions.size),
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

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = Spacings.BottomSheetContentPadding.calculateBottomPadding()),
            ) {
                items(
                    getReactingAuthorsForEmojiSelection(currentEmojiSelection, reactions),
                    key = { it.id }
                ) { reaction ->
                    ReactionAuthorListItem(
                        modifier = Modifier.animateItem(),
                        reaction = reaction
                    )
                }
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
            .padding(start = 8.dp),
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
    val isSelected = currentEmojiSelection == emojiSelection
    val shape = CircleShape
    val backgroundColor =
        if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.background

    Box(
        modifier = modifier
            .clip(shape)
            .background(color = backgroundColor, shape)
            .padding(horizontal = 8.dp, vertical = 5.dp)
            .clickable {
                onClick(emojiSelection)
            }
            .testTag(getTestTagForEmojiId(text, "REACTIONS_BOTTOM_SHEET")),
    ) {
        Text(
            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onBackground,
            text = if (emojiSelection is EmojiSelection.SINGLE) getUnicodeForEmojiId(emojiId = text) else text,
            fontSize = if (emojiSelection is EmojiSelection.SINGLE) 20.sp else 14.sp,
        )
    }
}

@Composable
private fun ReactionAuthorListItem(
    modifier: Modifier,
    reaction: ReactionAuthor
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .testTag(getTestTagForReactionAuthor(reaction.emojiId, reaction.id, reaction.username)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = getUnicodeForEmojiId(reaction.emojiId))

        Text(text = reaction.username)
    }
}

private fun getReactingAuthorsForEmojiSelection(
    emojiSelection: EmojiSelection,
    reactions: List<IReaction>
): List<ReactionAuthor> {
    val filteredReactions = if (emojiSelection is EmojiSelection.SINGLE) {
        reactions.filter { it.emojiId == emojiSelection.emojiId }
    } else {
        reactions
    }
    return filteredReactions.map { it as PostPojo.Reaction }
        .map { ReactionAuthor(it.id, it.username, it.emojiId) }
}