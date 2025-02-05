package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.create_personal_conversation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.alert.TextAlertDialog
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.JobAnimatedFloatingActionButton
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.NavigationBackButton
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.member_selection.MemberSelection
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

internal const val TEST_TAG_CREATE_PERSONAL_CONVERSATION_BUTTON = "create personal conversation button"

@Composable
fun CreatePersonalConversationScreen(
    modifier: Modifier,
    courseId: Long,
    onConversationCreated: (conversationId: Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    CreatePersonalConversationScreen(
        modifier = modifier,
        viewModel = koinViewModel { parametersOf(courseId) },
        onConversationCreated = onConversationCreated,
        onNavigateBack = onNavigateBack
    )
}

@Composable
internal fun CreatePersonalConversationScreen(
    modifier: Modifier,
    viewModel: CreatePersonalConversationViewModel,
    onConversationCreated: (conversationId: Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    val canCreateConversation by viewModel.canCreateConversation.collectAsState()

    var displayCreateConversationFailedDialog: Boolean by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            Surface(
                shadowElevation = Spacings.AppBarElevation
            ){
                TopAppBar(
                    title = { Text(text = stringResource(id = R.string.create_personal_conversation_title)) },
                    navigationIcon = {
                        NavigationBackButton(onNavigateBack)
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    )
                )
            }
        },
        floatingActionButton = {
            JobAnimatedFloatingActionButton(
                modifier = Modifier.testTag(TEST_TAG_CREATE_PERSONAL_CONVERSATION_BUTTON),
                enabled = canCreateConversation,
                startJob = viewModel::createConversation,
                onJobCompleted = { conversation ->
                    if (conversation != null) {
                        onConversationCreated(conversation.id)
                    } else {
                        displayCreateConversationFailedDialog = true
                    }
                }
            ) {
                Icon(imageVector = Icons.Default.Create, contentDescription = null)
            }
        }
    ) { padding ->
        MemberSelection(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(top = padding.calculateTopPadding() + Spacings.ScreenTopBarSpacing)
                .consumeWindowInsets(WindowInsets.systemBars.only(WindowInsetsSides.Top))
                .padding(horizontal = Spacings.ScreenHorizontalSpacing),
            viewModel = viewModel
        )
    }

    if (displayCreateConversationFailedDialog) {
        TextAlertDialog(
            title = stringResource(id = R.string.create_personal_conversation_failed_title),
            text = stringResource(id = R.string.create_personal_conversation_failed_message),
            confirmButtonText = stringResource(id = R.string.create_personal_conversation_failed_positive),
            dismissButtonText = null,
            onPressPositiveButton = { displayCreateConversationFailedDialog = false },
            onDismissRequest = { displayCreateConversationFailedDialog = false }
        )
    }
}
