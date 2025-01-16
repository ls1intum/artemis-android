package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.create_personal_conversation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardAlt
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
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
                .fillMaxWidth()
                .padding(horizontal = Spacings.ScreenHorizontalSpacing),
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
                modifier = Modifier.fillMaxSize(),
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
    FlowRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = inclusionList.students,
            onClick = {
                updateInclusionList(inclusionList.copy(students = !inclusionList.students))
            },
            label = { Text(text = stringResource(id = R.string.conversation_member_selection_include_students)) }
        )

        FilterChip(
            selected = inclusionList.tutors,
            onClick = {
                updateInclusionList(inclusionList.copy(tutors = !inclusionList.tutors))
            },
            label = { Text(text = stringResource(id = R.string.conversation_member_selection_include_tutors)) }
        )

        FilterChip(
            selected = inclusionList.instructors,
            onClick = {
                updateInclusionList(inclusionList.copy(instructors = !inclusionList.instructors))
            },
            label = { Text(text = stringResource(id = R.string.conversation_member_selection_include_instructors)) }
        )
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
        LazyColumn(
            modifier = modifier
        ) {
            items(recipients) { user ->
                CourseUserListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(testTagForPotentialRecipient(user.username.orEmpty())),
                    user = user,
                    trailingContent = {
                        IconButton(onClick = { addRecipient(user) }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(
                                    id = R.string.conversation_member_selection_content_description_add_recipient
                                )
                            )
                        }
                    }
                )
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

        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterVertically),
        ) {
            Icon(
                modifier = Modifier.size(64.dp),
                imageVector = icon,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                contentDescription = null
            )

            Text(
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                text = text,
            )
        }
    }
}