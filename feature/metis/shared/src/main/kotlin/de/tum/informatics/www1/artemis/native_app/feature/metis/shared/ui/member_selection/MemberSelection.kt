package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.member_selection

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicSearchTextField
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.member_selection.util.MemberSelectionItem
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.member_selection.util.MemberSelectionMode
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture.ProfilePicture
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture.ProfilePictureData

const val TEST_TAG_MEMBER_SELECTION_SEARCH_FIELD = "TEST_TAG_MEMBER_SELECTION_SEARCH_FIELD"
internal const val TEST_TAG_RECIPIENTS_LIST = "TEST_TAG_RECIPIENTS_LIST"

/**
 * Provides a member selection UI for selecting users or conversations.
 * By default only members can be selected.
 * If [isConversationSelectionEnabled] is set to true, conversations can be selected as well.
 * When selecting from conversations AND members, the [memberSelectionMode] must be set to
 * [MemberSelectionMode.MemberSelectionDropdown].
 */
@Composable
fun MemberSelection(
    modifier: Modifier,
    viewModel: MemberSelectionBaseViewModel,
    memberSelectionMode: MemberSelectionMode = MemberSelectionMode.MemberSelectionList,
    isConversationSelectionEnabled: Boolean = false,
    searchBarBackground: Color = MaterialTheme.colorScheme.surfaceContainer,
    onUpdateSelectedUserCount: (Int) -> Unit
) {
    val invalidRequirements = isConversationSelectionEnabled && memberSelectionMode is MemberSelectionMode.MemberSelectionList
    require(!invalidRequirements) { "Conversation selection is only enabled for the MemberSelectionDropdown mode" }

    val memberSelectionItems by viewModel.memberItems.collectAsState()
    val query by viewModel.query.collectAsState()
    val isQueryTooShort by viewModel.isQueryTooShort.collectAsState()
    val potentialRecipientsDataState by viewModel.potentialRecipients.collectAsState()
    val suggestions by viewModel.suggestions(isConversationSelectionEnabled).collectAsState()
    val inclusionList by viewModel.inclusionList.collectAsState()

    LaunchedEffect(memberSelectionItems) {
        onUpdateSelectedUserCount(memberSelectionItems.size)
    }

    Column(
        modifier = modifier,
    ) {
        RecipientsTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            memberSelectionActiveItems = memberSelectionItems,
            memberSelectionMode = memberSelectionMode,
            suggestions = suggestions,
            query = query,
            searchBarBackground = searchBarBackground,
            onUpdateQuery = viewModel::updateQuery,
            onRemoveMemberItem = viewModel::removeMemberItem,
            onAddMemberItem = viewModel::addMemberItem,
        )

        // Currently, conversation selection is only enabled for the MemberSelectionDropdown mode
        if (memberSelectionMode is MemberSelectionMode.MemberSelectionList) {
            PotentialRecipientsUi(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                potentialRecipientsDataState = potentialRecipientsDataState,
                isQueryTooShort = isQueryTooShort,
                inclusionList = inclusionList,
                onAddMemberItem = viewModel::addMemberItem,
                updateInclusionList = viewModel::updateInclusionList,
                retryLoadPotentialRecipients = viewModel::retryLoadPotentialRecipients
            )
            return
        }
    }
}

@Composable
private fun RecipientsTextField(
    modifier: Modifier,
    memberSelectionActiveItems: List<MemberSelectionItem>,
    memberSelectionMode: MemberSelectionMode,
    suggestions: List<MemberSelectionItem>,
    query: String,
    searchBarBackground: Color,
    onUpdateQuery: (String) -> Unit,
    onRemoveMemberItem: (MemberSelectionItem) -> Unit,
    onAddMemberItem: (MemberSelectionItem) -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AnimatedVisibility(visible = memberSelectionActiveItems.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .testTag(TEST_TAG_RECIPIENTS_LIST),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        memberSelectionActiveItems.forEach { item ->
                            Box {
                                MemberSelectionItemChip(
                                    modifier = Modifier,
                                    memberSelectionItem = item,
                                    onClickRemove = { onRemoveMemberItem(item) }
                                )
                            }
                        }
                    }
                }
            }
        }

        if(memberSelectionMode is MemberSelectionMode.MemberSelectionList){
            BasicSearchTextField(
                modifier = Modifier
                    .fillMaxWidth(),
                query = query,
                backgroundColor = searchBarBackground,
                updateQuery = onUpdateQuery,
                hint = stringResource(id = R.string.conversation_member_selection_address_hint),
                testTag = TEST_TAG_MEMBER_SELECTION_SEARCH_FIELD,
                focusRequester = focusRequester
            )
            return
        }

        MemberSelectionDropdownPopup(
            modifier = Modifier,
            query = query,
            suggestions = suggestions,
            focusRequester = focusRequester,
            searchBarBackground = searchBarBackground,
            onUpdateQuery = onUpdateQuery,
            onAddMemberItem = onAddMemberItem
        )
    }
}

@Composable
private fun MemberSelectionItemChip(
    modifier: Modifier,
    memberSelectionItem: MemberSelectionItem,
    onClickRemove: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable(onClick = onClickRemove)
            .testTag(memberSelectionItem.getTestTag())
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (memberSelectionItem is MemberSelectionItem.Recipient) {
                ProfilePicture(
                    modifier = Modifier.size(24.dp),
                    profilePictureData = ProfilePictureData.create(
                        imageUrl = memberSelectionItem.imageUrl,
                        username = memberSelectionItem.humanReadableName,
                        userId = memberSelectionItem.userId
                    )
                )
            } else if (memberSelectionItem is MemberSelectionItem.Conversation) {
                Icon(
                    imageVector = memberSelectionItem.imageVector,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Text(
                modifier = Modifier.padding(start = 8.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                text = memberSelectionItem.humanReadableName
            )
        }
    }
}
