package de.tum.informatics.www1.artemis.native_app.feature.metis.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import de.tum.informatics.www1.artemis.native_app.core.ui.AwaitDeferredCompletion
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.alert.TextAlertDialog
import de.tum.informatics.www1.artemis.native_app.core.ui.common.ButtonWithLoadingAnimation
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.MarkdownText
import de.tum.informatics.www1.artemis.native_app.feature.metis.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.humanReadableName
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred

@Composable
internal fun AcceptCodeOfConductUi(
    modifier: Modifier,
    codeOfConductText: String,
    responsibleUsers: List<User>,
    onRequestAccept: () -> Deferred<Boolean>
) {
    var acceptDeferred: Deferred<Boolean>? by remember { mutableStateOf(null) }
    var displayAcceptFailedDialog by remember { mutableStateOf(false) }

    AwaitDeferredCompletion(job = acceptDeferred) { successful ->
        acceptDeferred = null

        if (!successful) displayAcceptFailedDialog = true
    }

    // Simply display responsible users by appending corresponding markdown
    val codeOfConductTextWithResponsibleUsers: String = remember(responsibleUsers) {
        val responsibleUsersText =
            responsibleUsers.joinToString(separator = "\n") { responsibleUser ->
                "- ${responsibleUser.humanReadableName} (${responsibleUser.email})"
            }

        codeOfConductText + "\n" + responsibleUsersText
    }

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacings.ScreenHorizontalSpacing),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MarkdownText(
                modifier = Modifier.fillMaxWidth(),
                markdown = codeOfConductTextWithResponsibleUsers
            )

            ButtonWithLoadingAnimation(
                modifier = Modifier,
                isLoading = acceptDeferred != null,
                onClick = { acceptDeferred = onRequestAccept() }
            ) {
                Text(text = stringResource(id = R.string.code_of_conduct_button_accept))
            }
        }
    }

    if (displayAcceptFailedDialog) {
        TextAlertDialog(
            title = stringResource(id = R.string.code_of_conduct_accept_failed_dialog_title),
            text = stringResource(id = R.string.code_of_conduct_accept_failed_dialog_message),
            confirmButtonText = stringResource(id = R.string.code_of_conduct_accept_failed_dialog_positive),
            dismissButtonText = null,
            onPressPositiveButton = { displayAcceptFailedDialog = false },
            onDismissRequest = { displayAcceptFailedDialog = false }
        )
    }
}

private const val PreviewCodeOfConductText = """
# Code of Conduct Template: Adapt to your demands

We as students, tutors, and instructors pledge to make participation in our course a harassment-free experience for everyone, regardless of age, body size, visible or invisible disability, ethnicity, sex characteristics, gender identity and expression, level of experience, education, socio-economic status, nationality, personal appearance, race, religion, or sexual identity and orientation.

We pledge to act and interact in ways that contribute to an open, welcoming, diverse, inclusive, and healthy community.
"""

@Composable
@Preview
private fun AcceptCodeOfConductUiPreview() {
    Scaffold { padding ->
        AcceptCodeOfConductUi(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            codeOfConductText = PreviewCodeOfConductText,
            responsibleUsers = listOf(
                User(
                    firstName = "John",
                    lastName = "Appleseed",
                    email = "john.appleseed@example.com"
                ),
                User(
                    firstName = "Kate",
                    lastName = "Bell",
                    email = "kate.bell@example.com"
                ),
            ),
            onRequestAccept = ::CompletableDeferred
        )
    }
}