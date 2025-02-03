package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.add_members

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.ui.AwaitDeferredCompletion
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.alert.TextAlertDialog
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.JobAnimatedFloatingActionButton
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.NavigationBackButton
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.member_selection.MemberSelection
import kotlinx.coroutines.Deferred
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

internal const val TEST_TAG_ADD_MEMBERS_BUTTON = "add members to conversation button"

@Composable
fun ConversationAddMembersScreen(
    modifier: Modifier,
    courseId: Long,
    conversationId: Long,
    onNavigateBack: () -> Unit
) {
    ConversationAddMembersScreen(
        modifier = modifier,
        viewModel = koinViewModel { parametersOf(courseId, conversationId) },
        onNavigateBack
    )
}

@Composable
internal fun ConversationAddMembersScreen(
    modifier: Modifier,
    viewModel: ConversationAddMembersViewModel,
    onNavigateBack: () -> Unit
) {
    val canAdd by viewModel.canAdd.collectAsState()
    var addDeferred: Deferred<Boolean>? by remember { mutableStateOf(null) }

    var displayAddMembersFailedDialog by remember { mutableStateOf(false) }
    var numberOfSelectedUsers by remember { mutableIntStateOf(0) }

    AwaitDeferredCompletion(job = addDeferred) { successful ->
        addDeferred = null

        if (successful) {
            onNavigateBack()
        } else {
            displayAddMembersFailedDialog = true
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.conversation_add_members_title)) },
                navigationIcon = {
                    NavigationBackButton(
                        imageVector = Icons.Default.Close,
                        onNavigateBack = onNavigateBack
                    )
                }
            )
        },
        floatingActionButton = {
            JobAnimatedFloatingActionButton(
                modifier = Modifier
                    .imePadding()
                    .testTag(TEST_TAG_ADD_MEMBERS_BUTTON),
                enabled = canAdd,
                startJob = viewModel::addMembers,
                onJobCompleted = { successful ->
                    if (successful) {
                        onNavigateBack()
                    } else {
                        displayAddMembersFailedDialog = true
                    }
                }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = pluralStringResource(
                            R.plurals.create_personal_conversation_members,
                            numberOfSelectedUsers,
                            numberOfSelectedUsers
                        )
                    )

                    Icon(
                        modifier = Modifier
                            .padding(start = 8.dp),
                        imageVector = Icons.Default.Done,
                        contentDescription = null
                    )
                }
            }
        }
    ) { paddingValues ->
        MemberSelection(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(top = paddingValues.calculateTopPadding())
                .padding(bottom = paddingValues.calculateBottomPadding())
                .padding(horizontal = Spacings.ScreenHorizontalSpacing)
                .consumeWindowInsets(WindowInsets.systemBars.only(WindowInsetsSides.Top)),
            viewModel = viewModel,
            onUpdateSelectedUserCount = { numberOfSelectedUsers = it }
        )
    }

    if (displayAddMembersFailedDialog) {
        TextAlertDialog(
            title = stringResource(id = R.string.conversation_add_members_failed_dialog_title),
            text = stringResource(id = R.string.conversation_add_members_failed_dialog_message),
            confirmButtonText = stringResource(id = R.string.conversation_add_members_failed_dialog_positive),
            dismissButtonText = null,
            onPressPositiveButton = { displayAddMembersFailedDialog = false },
            onDismissRequest = { displayAddMembersFailedDialog = false }
        )
    }
}