package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.saved_posts.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BottomSheetActionButton
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.saved_posts.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.service.MetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.ui.ChatListItem
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.ui.MetisModificationTaskHandler
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.ISavedPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.SavedPostStatus
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.getIcon
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.getTintColor
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.getUiText
import kotlinx.coroutines.Deferred


@Composable
fun SavedPostWithActions(
    modifier: Modifier = Modifier,
    savedPostChatListItem: ChatListItem.PostItem.SavedItem,
    onClick: () -> Unit,
    onChangeStatus: (newStatus: SavedPostStatus) -> Deferred<MetisModificationFailure?>,
    onRemoveFromSavedPosts: () -> Deferred<MetisModificationFailure?>
) {
    var displayBottomSheet by remember { mutableStateOf(false) }
    var metisModificationTask by remember {
        mutableStateOf<Deferred<MetisModificationFailure?>?>(null)
    }

    MetisModificationTaskHandler(
        metisModificationTask = metisModificationTask,
        onTaskCompletion = { metisModificationTask = null }
    )

    Column(modifier = modifier) {
        SavedPostItem(
            modifier = Modifier.fillMaxWidth(),
            savedPostChatListItem = savedPostChatListItem,
            isLoading = metisModificationTask != null,
            onClick = onClick,
            onLongClick = { displayBottomSheet = true },
            trailingCardContent = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (status in possibleNextStatuses((savedPostChatListItem.post as ISavedPost).savedPostStatus)) {
                        StatusActionButton(
                            modifier = Modifier
                                .weight(1f)
                                .align(Alignment.CenterVertically),
                            status = status,
                            onClick = {
                                metisModificationTask = onChangeStatus(status)
                            }
                        )
                    }
                }
            }
        )
    }

    if (displayBottomSheet) {
        SavedPostBottomSheet(
            currentStatus = (savedPostChatListItem.post as ISavedPost).savedPostStatus,
            onChangeStatusActionClick = { newStatus ->
                metisModificationTask = onChangeStatus(newStatus)
                displayBottomSheet = false
            },
            onRemoveClick = {
                metisModificationTask = onRemoveFromSavedPosts()
                displayBottomSheet = false
            },
            onDismissRequest = { displayBottomSheet = false }
        )
    }
}

@Composable
private fun SavedPostBottomSheet(
    currentStatus: SavedPostStatus,
    onChangeStatusActionClick: (SavedPostStatus) -> Unit,
    onRemoveClick: () -> Unit,
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
                    id = R.string.saved_posts_action_change_status,
                    status.getUiText()
                )

                BottomSheetActionButton(
                    text = text,
                    icon = status.getIcon(),
                    onClick = { onChangeStatusActionClick(status) }
                )
            }

            BottomSheetActionButton(
                text = stringResource(id = R.string.saved_posts_action_remove),
                icon = Icons.Default.Delete,
                onClick = onRemoveClick
            )
        }
    }
}

private fun possibleNextStatuses(current: SavedPostStatus): List<SavedPostStatus> {
    return when (current) {
        SavedPostStatus.IN_PROGRESS -> listOf(
            SavedPostStatus.COMPLETED,
            SavedPostStatus.ARCHIVED
        )

        SavedPostStatus.COMPLETED -> listOf(
            SavedPostStatus.IN_PROGRESS,
            SavedPostStatus.ARCHIVED
        )

        SavedPostStatus.ARCHIVED -> listOf(
            SavedPostStatus.IN_PROGRESS,
            SavedPostStatus.COMPLETED
        )
    }
}

@Composable
private fun StatusActionButton(
    modifier: Modifier = Modifier,
    status: SavedPostStatus,
    onClick: () -> Unit
) {
    val backgroundColor = status.getTintColor().copy(alpha = 0.8f)

    Button(
        modifier = modifier,
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = Color.White
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = status.getIcon(),
                contentDescription = null
            )
            Text(text = stringResource(status.getActionButtonText()))
        }
    }
}

private fun SavedPostStatus.getActionButtonText(): Int {
    return when (this) {
        SavedPostStatus.IN_PROGRESS -> R.string.saved_posts_action_mark_as_in_progress
        SavedPostStatus.COMPLETED -> R.string.saved_posts_action_mark_as_completed
        SavedPostStatus.ARCHIVED -> R.string.saved_posts_action_mark_as_archive
    }
}

