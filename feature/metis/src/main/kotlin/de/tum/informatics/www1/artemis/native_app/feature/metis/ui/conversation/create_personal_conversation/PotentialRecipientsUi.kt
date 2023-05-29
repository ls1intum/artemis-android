package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.create_personal_conversation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.feature.metis.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.humanReadableName

@Composable
internal fun PotentialRecipientsUi(
    modifier: Modifier,
    potentialRecipientsDataState: DataState<List<User>>,
    inclusionList: InclusionList,
    addRecipient: (User) -> Unit,
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
                addRecipient = addRecipient
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
    recipients: List<User>,
    addRecipient: (User) -> Unit
) {
    if (recipients.isNotEmpty()) {
        LazyColumn(modifier = modifier) {
            items(recipients) { user ->
                ListItem(
                    modifier = Modifier.fillMaxWidth(),
                    headlineContent = {
                        val username = remember(user) { user.humanReadableName }

                        Text(username)
                    },
                    supportingContent = user.username?.let { username -> { Text(username) } },
                    trailingContent = {
                        IconButton(onClick = { addRecipient(user) }) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        }
                    }
                )
            }
        }
    } else {
        Box(modifier = modifier) {
            Text(
                modifier = Modifier.align(Alignment.TopCenter),
                text = stringResource(id = R.string.conversation_member_selection_recipients_empty)
            )
        }
    }
}