package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.create_personal_conversation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.alert.TextAlertDialog
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.JobAnimatedFloatingActionButton
import de.tum.informatics.www1.artemis.native_app.feature.metis.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.courseNavGraphBuilderExtensions
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.detail.navigateToConversationDetailScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.member_selection.MemberSelection
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.member_selection.Recipient
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
    courseNavGraphBuilderExtensions(
        route = "course/{courseId}/create_personal_conversation"
    ) { courseId ->
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

    val canCreateConversation by viewModel.canCreateConversation.collectAsState()

    var displayCreateConversationFailedDialog: Boolean by remember { mutableStateOf(false) }

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
        },
        floatingActionButton = {
            JobAnimatedFloatingActionButton(
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
                .padding(padding)
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
