package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.member_selection

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicHintTextField
import de.tum.informatics.www1.artemis.native_app.feature.metis.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.create_personal_conversation.PotentialRecipientsUi

internal const val TEST_TAG_MEMBER_SELECTION_SEARCH_FIELD = "member selection search field"

@Composable
internal fun MemberSelection(
    modifier: Modifier,
    viewModel: MemberSelectionBaseViewModel
) {
    val recipients by viewModel.recipients.collectAsState()
    val query by viewModel.query.collectAsState()
    val potentialRecipientsDataState by viewModel.potentialRecipients.collectAsState()
    val inclusionList by viewModel.inclusionList.collectAsState()

    Column(
        modifier = modifier,
    ) {
        RecipientsTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacings.ScreenHorizontalSpacing)
                .padding(bottom = 4.dp),
            recipients = recipients,
            query = query,
            onUpdateQuery = viewModel::updateQuery,
            onRemoveRecipient = viewModel::removeRecipient
        )

        Divider()

        PotentialRecipientsUi(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            potentialRecipientsDataState = potentialRecipientsDataState,
            inclusionList = inclusionList,
            addRecipient = viewModel::addRecipient,
            updateInclusionList = viewModel::updateInclusionList,
            retryLoadPotentialRecipients = viewModel::retryLoadPotentialRecipients
        )
    }
}

@Composable
private fun RecipientsTextField(
    modifier: Modifier,
    recipients: List<Recipient>,
    query: String,
    onUpdateQuery: (String) -> Unit,
    onRemoveRecipient: (Recipient) -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AnimatedVisibility(visible = recipients.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = stringResource(id = R.string.conversation_member_selection_address_label))

                Column(modifier = Modifier.fillMaxWidth()) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        mainAxisSpacing = 8.dp,
                        crossAxisSpacing = 8.dp
                    ) {
                        recipients.forEach { recipient ->
                            Box {
                                RecipientChip(
                                    modifier = Modifier,
                                    recipient = recipient,
                                    onClickRemove = { onRemoveRecipient(recipient) }
                                )
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = stringResource(id = R.string.conversation_member_selection_search_label))

            val textStyle = LocalTextStyle.current

            BasicHintTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .testTag(TEST_TAG_MEMBER_SELECTION_SEARCH_FIELD),
                value = query,
                onValueChange = onUpdateQuery,
                hint = stringResource(id = R.string.conversation_member_selection_address_hint),
                maxLines = 1,
                hintStyle = remember(textStyle) { textStyle.copy(textStyle.color.copy(alpha = 0.8f)) }
            )
        }
    }
}

@Composable
private fun RecipientChip(modifier: Modifier, recipient: Recipient, onClickRemove: () -> Unit) {
    Box(
        modifier = modifier
            .border(1.dp, color = MaterialTheme.colorScheme.outline, CircleShape)
            .clip(CircleShape)
            .clickable(onClick = onClickRemove)
    ) {
        Row(modifier = Modifier.padding(2.dp)) {
            Text(modifier = Modifier.padding(start = 8.dp), text = recipient.humanReadableName)

            Icon(imageVector = Icons.Default.Close, contentDescription = null)
        }
    }

}