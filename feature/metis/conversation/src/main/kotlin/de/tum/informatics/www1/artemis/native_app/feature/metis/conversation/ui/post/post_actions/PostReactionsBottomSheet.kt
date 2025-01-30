package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.getUnicodeForEmojiId
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IBasePost

internal const val TEST_TAG_POST_REACTIONS_BOTTOM_SHEET = "TEST_TAG_POST_REACTIONS_BOTTOM_SHEET"

@Composable
fun PostReactionBottomSheet(
    post: IBasePost,
    selectedEmojiId: String,
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
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = getUnicodeForEmojiId(selectedEmojiId)
            )
        }
    }
}