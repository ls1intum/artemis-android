package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.saved_posts.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.saved_posts.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.service.MetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.ui.BottomSheetActionButton
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.ui.MetisModificationTaskHandler
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.ISavedPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.SavedPostStatus
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.getIcon
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.getUiText
import kotlinx.coroutines.Deferred


@Composable
fun SavedPostWithActions(
    modifier: Modifier = Modifier,
    savedPost: ISavedPost,
    onClick: () -> Unit,
    onChangeStatus: (newStatus: SavedPostStatus) -> Deferred<MetisModificationFailure?>,
    onRemoveFromSavedPosts: () -> Deferred<MetisModificationFailure?>
) {
    var displayBottomSheet by remember { mutableStateOf(false) }

    var metisModificationTask by remember {
        mutableStateOf<Deferred<MetisModificationFailure?>?>(
            null
        )
    }
    MetisModificationTaskHandler(
        metisModificationTask = metisModificationTask,
        onTaskCompletion = {
            metisModificationTask = null
        }
    )

    Column(
        modifier = modifier,
    ) {
        SavedPostItem(
            modifier = Modifier.fillMaxWidth(),
            savedPost = savedPost,
            isLoading = metisModificationTask != null,
            onClick = onClick,
            onLongClick = {
                displayBottomSheet = true
            },
            trailingCardContent = {
                if (savedPost.savedPostStatus == SavedPostStatus.IN_PROGRESS) {
                    CompleteButton(
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 4.dp),
                        onClick = {
                            metisModificationTask = onChangeStatus(SavedPostStatus.COMPLETED)
                        }
                    )
                }
            }
        )
    }


    if (displayBottomSheet) {
        SavedPostBottomSheet(
            currentStatus = savedPost.savedPostStatus,
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
private fun CompleteButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        modifier = modifier,
        onClick = onClick
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckBox,
                contentDescription = null
            )
            Text(text = stringResource(id = R.string.saved_posts_action_mark_as_completed))
        }
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
                if (status == currentStatus) { continue }

                val text = stringResource(
                    id = R.string.saved_posts_action_change_status,
                    status.getUiText()
                )

                BottomSheetActionButton(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = text,
                    icon = status.getIcon(),
                    onClick = { onChangeStatusActionClick(status) }
                )
            }

            BottomSheetActionButton(
                modifier = Modifier
                    .fillMaxWidth(),
                text = stringResource(id = R.string.saved_posts_action_remove),
                icon = Icons.Default.Delete,
                onClick = onRemoveClick
            )
        }
    }
}