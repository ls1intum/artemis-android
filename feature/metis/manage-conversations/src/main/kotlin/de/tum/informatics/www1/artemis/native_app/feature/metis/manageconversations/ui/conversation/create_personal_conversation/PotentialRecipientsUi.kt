package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.create_personal_conversation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardAlt
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.common.EmptyListHint
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.common.CourseUserListItem
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.member_selection.MemberSelectionBaseViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.CourseUser

internal fun testTagForPotentialRecipient(username: String) = "potentialRecipient$username"

@Composable
internal fun PotentialRecipientsUi(
    modifier: Modifier,
    potentialRecipientsDataState: DataState<List<CourseUser>>,
    isQueryTooShort: Boolean,
    inclusionList: InclusionList,
    addRecipient: (CourseUser) -> Unit,
    updateInclusionList: (InclusionList) -> Unit,
    retryLoadPotentialRecipients: () -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        InclusionListUi(
            modifier = Modifier
                .fillMaxWidth(),
            inclusionList = inclusionList,
            updateInclusionList = updateInclusionList
        )

        BasicDataStateUi(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            dataState = potentialRecipientsDataState,
            loadingText = stringResource(id = R.string.conversation_member_selection_load_recipients_loading),
            failureText = stringResource(id = R.string.conversation_member_selection_load_recipients_failure),
            retryButtonText = stringResource(id = R.string.conversation_member_selection_load_recipients_try_again),
            onClickRetry = retryLoadPotentialRecipients
        ) { potentialRecipients ->
            PotentialRecipientsList(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding(),
                recipients = potentialRecipients,
                addRecipient = addRecipient,
                isQueryTooShort = isQueryTooShort
            )
        }
    }
}

@Composable
private fun InclusionListUi(
    modifier: Modifier,
    inclusionList: InclusionList,
    updateInclusionList: (InclusionList) -> Unit
) {
    val chip: @Composable (Boolean, Int, () -> Unit) -> Unit = { selected, labelResId, onClick ->
        FilterChip(
            selected = selected,
            onClick = onClick,
            leadingIcon = {
                if (selected) Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = Icons.Filled.Check,
                    contentDescription = null
                )
            },
            label = { Text(text = stringResource(id = labelResId)) }
        )
    }

    FlowRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        chip(inclusionList.students, R.string.conversation_member_selection_include_students) {
            updateInclusionList(inclusionList.copy(students = !inclusionList.students))
        }

        chip(inclusionList.tutors, R.string.conversation_member_selection_include_tutors) {
            updateInclusionList(inclusionList.copy(tutors = !inclusionList.tutors))
        }

        chip(
            inclusionList.instructors,
            R.string.conversation_member_selection_include_instructors
        ) {
            updateInclusionList(inclusionList.copy(instructors = !inclusionList.instructors))
        }
    }
}

@Composable
private fun PotentialRecipientsList(
    modifier: Modifier,
    recipients: List<CourseUser>,
    addRecipient: (CourseUser) -> Unit,
    isQueryTooShort: Boolean
) {
    if (recipients.isNotEmpty()) {
        Card {
            // We need to add a bottom contentPadding of 90.dp here to prevent the last item from being hidden behind the FAB
            LazyColumn(
                modifier = modifier
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 90.dp),
            ) {
                items(recipients) { user ->
                    CourseUserListItem(
                        modifier = Modifier
                            .animateItem()
                            .testTag(testTagForPotentialRecipient(user.username.orEmpty())),
                        user = user,
                        trailingContent = {
                            IconButton(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .size(32.dp)
                                    .background(MaterialTheme.colorScheme.surfaceContainer),
                                onClick = { addRecipient(user) }
                            ) {
                                Icon(
                                    modifier = Modifier.size(24.dp),
                                    imageVector = Icons.Default.Add,
                                    tint = MaterialTheme.colorScheme.primary,
                                    contentDescription = stringResource(
                                        id = R.string.conversation_member_selection_content_description_add_recipient
                                    )
                                )
                            }
                        }
                    )

                    HorizontalDivider()
                }
            }
        }
    } else {
        val icon = if (isQueryTooShort) Icons.Default.KeyboardAlt else Icons.Default.PersonOff
        val text = if (isQueryTooShort) {
            stringResource(
                id = R.string.conversation_member_selection_query_too_short,
                MemberSelectionBaseViewModel.MINIMUM_QUERY_LENGTH
            )
        } else {
            stringResource(id = R.string.conversation_member_selection_recipients_empty)
        }

        EmptyListHint(
            modifier,
            icon = icon,
            hint = text,
        )
    }
}