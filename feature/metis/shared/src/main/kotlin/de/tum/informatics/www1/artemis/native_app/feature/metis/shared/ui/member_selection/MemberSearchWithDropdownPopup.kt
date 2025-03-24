package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.member_selection

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicSearchTextField
import de.tum.informatics.www1.artemis.native_app.core.ui.common.EmptyListHint
import de.tum.informatics.www1.artemis.native_app.core.ui.common.NoSearchResults
import de.tum.informatics.www1.artemis.native_app.core.ui.common.top_app_bar.dropShadowBelow
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.member_selection.util.MemberSelectionItem
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture.ProfilePicture
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture.ProfilePictureData
import kotlin.math.absoluteValue

@Composable
fun MemberSearchWithDropdownPopup(
    modifier: Modifier,
    query: String,
    focusRequester: FocusRequester,
    suggestions: List<MemberSelectionItem>,
    searchBarBackground: Color,
    onUpdateQuery: (String) -> Unit,
    onAddMemberItem: (MemberSelectionItem) -> Unit
) {
    var isPopupExpanded by remember { mutableStateOf(true) }
    val conversations = suggestions.filterIsInstance<MemberSelectionItem.Conversation>()
    val recipients = suggestions.filterIsInstance<MemberSelectionItem.Recipient>()

    BasicSearchTextField(
        modifier = modifier
            .onFocusChanged { focusState ->
                isPopupExpanded = focusState.isFocused && suggestions.isNotEmpty()
            }
            .fillMaxWidth(),
        query = query,
        backgroundColor = searchBarBackground,
        updateQuery = {
            onUpdateQuery(it)
            val newExpanded = query.isNotBlank() && suggestions.isNotEmpty()
            // Only update when it actually changes to avoid unnecessary recompositions and popup shaking
            if (isPopupExpanded != newExpanded) {
                isPopupExpanded = newExpanded
            }
        },
        hint = stringResource(id = R.string.conversation_member_or_channel_selection_address_hint),
        focusRequester = focusRequester
    )

    if (!isPopupExpanded || suggestions.isEmpty()) return

    var boxWidth by remember { mutableIntStateOf(0) }
    var boxYOffset by remember { mutableIntStateOf(0) }

    val localConfiguration = LocalConfiguration.current
    val localDensity = LocalDensity.current
    val imeInsets = WindowInsets.ime.getBottom(localDensity)
    Box(
        modifier = Modifier
            .onSizeChanged {
                boxWidth = it.width
            }
            .onGloballyPositioned { coordinates ->
                val boxBottomY = coordinates.positionInWindow().y + coordinates.size.height
                val screenHeightPx = with(localDensity) { localConfiguration.screenHeightDp.dp.toPx() }
                boxYOffset = (boxBottomY - screenHeightPx + imeInsets).toInt().absoluteValue
            }
            .fillMaxWidth()
    ) {
        val maxHeightFromScreen = with(LocalDensity.current) { boxYOffset.toDp() }
        val targetWidth = with(LocalDensity.current) { boxWidth.toDp() }
        val maxHeight = min(maxHeightFromScreen, Spacings.Popup.maxHeight)
        val height = max(maxHeight, Spacings.Popup.minHeight)

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
            onDismissRequest = { isPopupExpanded = false }
        ) {
            PopupContent(
                conversations = conversations,
                recipients = recipients,
                query = query,
                height = height,
                targetWidth = targetWidth,
                onDismissRequest = {
                    isPopupExpanded = false
                    onUpdateQuery("")
                },
                onAddMemberItem = onAddMemberItem
            )
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
private fun PopupContent(
    conversations: List<MemberSelectionItem.Conversation>,
    recipients: List<MemberSelectionItem.Recipient>,
    query: String,
    height: Dp,
    targetWidth: Dp,
    onDismissRequest: () -> Unit,
    onAddMemberItem: (MemberSelectionItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .padding(horizontal = Spacings.ScreenHorizontalSpacing)
            .height(height)
            .width(targetWidth)
            .dropShadowBelow()
            .clip(MaterialTheme.shapes.large)
            .background(color = MaterialTheme.colorScheme.surfaceContainerHighest)
            .padding(8.dp)
    ) {
        stickyHeader {
            PopupHeader(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(id = R.string.conversation_member_selection_channels)
            )
        }

        if (conversations.isEmpty()) {
            item {
                NoSearchResults(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(id = R.string.conversation_member_selection_channels_no_search_results_title),
                    details = stringResource(
                        id = R.string.conversation_member_selection_channels_no_search_results_details,
                        query
                    ),
                    showIcon = false
                )
            }
        } else {
            conversations.forEach { item ->
                item {
                    PopupItem(
                        modifier = Modifier.fillMaxWidth(),
                        item = item,
                        onAddMemberItem = onAddMemberItem,
                        onDismissRequest = onDismissRequest
                    )
                }
            }
        }

        stickyHeader {
            PopupHeader(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(id = R.string.conversation_member_selection_users)
            )
        }

        if (recipients.isEmpty() && query.length < MemberSelectionBaseViewModel.MINIMUM_QUERY_LENGTH) {
            item {
                EmptyListHint(
                    modifier = Modifier.fillMaxWidth(),
                    imageVector = Icons.Default.Keyboard,
                    hint = stringResource(id = R.string.conversation_member_selection_query_too_short_popup,
                        MemberSelectionBaseViewModel.MINIMUM_QUERY_LENGTH
                    )
                )
            }
        } else if (recipients.isEmpty()) {
            item {
                NoSearchResults(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(id = R.string.conversation_member_selection_recipients_no_search_results_title),
                    details = stringResource(id = R.string.conversation_member_selection_recipients_no_search_results_details, query),
                    showIcon = false
                )
            }
        } else {
            recipients.forEach { item ->
                item {
                    PopupItem(
                        modifier = Modifier.fillMaxWidth(),
                        item = item,
                        onAddMemberItem = onAddMemberItem,
                        onDismissRequest = onDismissRequest
                    )
                }
            }
        }
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