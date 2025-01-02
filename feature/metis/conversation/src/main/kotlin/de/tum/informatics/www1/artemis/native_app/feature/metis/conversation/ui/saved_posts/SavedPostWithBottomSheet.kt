package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.saved_posts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions.BottomSheetActionButton
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.ISavedPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.SavedPostStatus
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.getIcon
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.getUiText


@Composable
fun SavedPostWithBottomSheet(
    modifier: Modifier = Modifier,
    savedPost: ISavedPost,
    onClick: () -> Unit,
    onChangeStatus: (newStatus: SavedPostStatus) -> Unit,
) {
    var displayBottomSheet by remember(savedPost) { mutableStateOf(false) }

    SavedPostItem(
        modifier = modifier,
        savedPost = savedPost,
        onClick = onClick,
        onLongClick = {
            displayBottomSheet = true
        },
    )

    if (displayBottomSheet) {
        SavedPostBottomSheet(
            currentStatus = savedPost.savedPostStatus,
            onActionClick = { newStatus ->
                onChangeStatus(newStatus)
                displayBottomSheet = false
            },
            onDismissRequest = { displayBottomSheet = false }
        )
    }
}

@Composable
private fun SavedPostBottomSheet(
    currentStatus: SavedPostStatus,
    onActionClick: (SavedPostStatus) -> Unit,
    onDismissRequest: () -> Unit
) {
    ModalBottomSheet(
        contentWindowInsets = { WindowInsets.statusBars },
        sheetState = rememberModalBottomSheetState(),
        onDismissRequest = onDismissRequest
    ) {
        Column(
            modifier = Modifier.padding(Spacings.BottomSheetContentPadding)
        ) {
            for (status in SavedPostStatus.entries) {
                if (status == currentStatus) {
                    continue
                }

                val text = stringResource(
                    id = R.string.saved_posts_change_status,
                    status.getUiText()
                )

                BottomSheetActionButton(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = text,
                    icon = status.getIcon(),
                    onClick = { onActionClick(status) }
                )
            }
        }
    }
}