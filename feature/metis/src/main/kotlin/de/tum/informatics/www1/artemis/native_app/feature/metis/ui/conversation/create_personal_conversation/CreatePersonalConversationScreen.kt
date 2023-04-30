package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.create_personal_conversation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.accompanist.flowlayout.FlowRow
import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicHintTextField
import de.tum.informatics.www1.artemis.native_app.feature.metis.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.detail.navigateToConversationDetailScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.humanReadableName
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavController.navigateToCreatePersonalConversationScreen(
    courseId: Long,
    builder: NavOptionsBuilder.() -> Unit
) {
    navigate("course/$courseId/create_personal_conversation", builder)
}

fun NavGraphBuilder.createPersonalConversationScreen(
    navController: NavController,
    onNavigateBack: () -> Unit
) {
    composable(
        route = "course/{courseId}/create_personal_conversation",
        arguments = listOf(
            navArgument("courseId") { type = NavType.LongType; nullable = false }
        )
    ) { backStackEntry ->
        val courseId =
            backStackEntry.arguments?.getLong("courseId")
        checkNotNull(courseId)

        CreatePersonalConversationScreen(
            modifier = Modifier.fillMaxSize(),
            courseId = courseId,
            onConversationCreated = { conversationId ->
                navController.popBackStack()
                navController.navigateToConversationDetailScreen(courseId, conversationId) { }
            },
            onNavigateBack = onNavigateBack
        )
    }
}

@Composable
private fun CreatePersonalConversationScreen(
    modifier: Modifier,
    courseId: Long,
    onConversationCreated: (conversationId: Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    val viewModel: CreatePersonalConversationViewModel = koinViewModel { parametersOf(courseId) }
    val query: String by viewModel.query.collectAsState()
    val inclusionList: InclusionList by viewModel.inclusionList.collectAsState()
    val recipients: List<User> by viewModel.recipients.collectAsState()

    val potentialRecipientsDataState by viewModel.potentialRecipients.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.create_personal_conversation_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Spacings.ScreenHorizontalSpacing)
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
}

@Composable
private fun RecipientsTextField(
    modifier: Modifier,
    recipients: List<User>,
    query: String,
    onUpdateQuery: (String) -> Unit,
    onRemoveRecipient: (User) -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = stringResource(id = R.string.create_personal_conversation_address_label))

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

            val textStyle = LocalTextStyle.current

            BasicHintTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                value = query,
                onValueChange = onUpdateQuery,
                hint = stringResource(id = R.string.create_personal_conversation_address_hint),
                maxLines = 1,
                hintStyle = remember(textStyle) { textStyle.copy(textStyle.color.copy(alpha = 0.8f)) }
            )
        }
    }
}

@Composable
private fun RecipientChip(modifier: Modifier, recipient: User, onClickRemove: () -> Unit) {
    val name = remember(recipient) { recipient.humanReadableName }

    Box(
        modifier = modifier
            .border(1.dp, color = MaterialTheme.colorScheme.outline, CircleShape)
            .clip(CircleShape)
            .clickable(onClick = onClickRemove)
    ) {
        Row(modifier = Modifier.padding(2.dp)) {
            Text(modifier = Modifier.padding(start = 8.dp), text = name)

            Icon(imageVector = Icons.Default.Close, contentDescription = null)
        }
    }

}