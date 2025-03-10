package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.member_selection

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicSearchTextField
import de.tum.informatics.www1.artemis.native_app.core.ui.common.top_app_bar.dropShadowBelow
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.member_selection.util.MemberSelectionItem
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture.ProfilePicture
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture.ProfilePictureData

@Composable
fun MemberSelectionDropdownPopup(
    modifier: Modifier,
    query: String,
    focusRequester: FocusRequester,
    suggestions: List<MemberSelectionItem>,
    onUpdateQuery: (String) -> Unit,
    onAddMemberItem: (MemberSelectionItem) -> Unit
) {
    var expanded by remember { mutableStateOf(true) }
    val conversations = suggestions.filterIsInstance<MemberSelectionItem.Conversation>()
    val recipients = suggestions.filterIsInstance<MemberSelectionItem.Recipient>()

    BasicSearchTextField(
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                expanded = focusState.isFocused && suggestions.isNotEmpty()
            },
        query = query,
        updateQuery = {
            onUpdateQuery(it)
            expanded = it.isNotBlank() && suggestions.isNotEmpty()
        },
        hint = stringResource(id = R.string.conversation_member_or_conversation_selection_address_hint),
        testTag = TEST_TAG_MEMBER_SELECTION_SEARCH_FIELD,
        focusRequester = focusRequester
    )

    if (expanded && suggestions.isNotEmpty()) {
        val screenHeight = LocalConfiguration.current.screenHeightDp.dp
        val maxHeightFromScreen = screenHeight * 0.4f
        val maxHeight = min(maxHeightFromScreen, Spacings.Popup.maxHeight)

        Popup(
            popupPositionProvider = object : PopupPositionProvider {
                override fun calculatePosition(
                    anchorBounds: IntRect,
                    windowSize: IntSize,
                    layoutDirection: LayoutDirection,
                    popupContentSize: IntSize
                ): IntOffset = anchorBounds.bottomLeft + IntOffset(0, 8)
            },
            properties = PopupProperties(dismissOnClickOutside = true),
            onDismissRequest = { expanded = false }
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacings.ScreenHorizontalSpacing)
                    .heightIn(max = maxHeight)
                    .dropShadowBelow()
                    .clip(MaterialTheme.shapes.large)
                    .background(color = MaterialTheme.colorScheme.surfaceContainerHighest)
                    .padding(8.dp)
            ) {
                stickyHeader {
                    PopupHeader(
                        modifier = Modifier.fillMaxWidth(),
                        title = stringResource(id = R.string.conversation_member_selection_conversations)
                    )
                }

                conversations.forEach { item ->
                    item {
                        PopupItem(
                            modifier = Modifier.fillMaxWidth(),
                            item = item,
                            onAddMemberItem = onAddMemberItem,
                            onDismissRequest = { expanded = false }
                        )
                    }
                }

                stickyHeader {
                    PopupHeader(
                        modifier = Modifier.fillMaxWidth(),
                        title = stringResource(id = R.string.conversation_member_selection_users)
                    )
                }

                recipients.forEach { item ->
                    item {
                        PopupItem(
                            modifier = Modifier.fillMaxWidth(),
                            item = item,
                            onAddMemberItem = onAddMemberItem,
                            onDismissRequest = { expanded = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PopupHeader(
    modifier: Modifier,
    title: String
) {
    Box(modifier) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colorScheme.surfaceContainerHighest)
                .padding(horizontal = Spacings.Popup.HintHorizontalPadding, vertical = 4.dp),
            text = title,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PopupItem(
    modifier: Modifier,
    item: MemberSelectionItem,
    onAddMemberItem: (MemberSelectionItem) -> Unit,
    onDismissRequest: () -> Unit
) {
    Row(
        modifier = modifier
            .clickable {
                onAddMemberItem(item)
                onDismissRequest()
            }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (item is MemberSelectionItem.Recipient) {
            ProfilePicture(
                modifier = Modifier.size(24.dp),
                profilePictureData = ProfilePictureData.create(
                    imageUrl = item.imageUrl,
                    username = item.humanReadableName,
                    userId = item.userId
                )
            )
        } else if (item is MemberSelectionItem.Conversation){
            Icon(
                imageVector = item.imageVector,
                contentDescription = null,
            )
        }

        Text(item.humanReadableName)
    }
}