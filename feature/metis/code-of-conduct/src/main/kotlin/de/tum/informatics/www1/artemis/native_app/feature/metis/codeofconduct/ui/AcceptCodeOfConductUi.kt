package de.tum.informatics.www1.artemis.native_app.feature.metis.codeofconduct.ui

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import de.tum.informatics.www1.artemis.native_app.core.ui.AwaitDeferredCompletion
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.alert.TextAlertDialog
import de.tum.informatics.www1.artemis.native_app.core.ui.common.ButtonWithLoadingAnimation
import de.tum.informatics.www1.artemis.native_app.feature.metis.codeofconduct.R
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred

@Composable
fun AcceptCodeOfConductUi(
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

    Box(modifier = modifier) {
        var parentHeight by remember { mutableIntStateOf(0) }
        var buttonPositionInParent by remember { mutableFloatStateOf(0f) }
        var buttonHeightInParent by remember { mutableIntStateOf(0) }

        val scrollState = rememberScrollState()

        val isButtonVisible =
            scrollState.value + parentHeight - buttonHeightInParent / 2f >= buttonPositionInParent

        Column(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned {
                    parentHeight = it.size.height
                }
                .verticalScroll(scrollState)
                .padding(horizontal = Spacings.ScreenHorizontalSpacing),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CodeOfConductUi(
                modifier = Modifier.fillMaxSize(),
                codeOfConductText = codeOfConductText,
                responsibleUsers = responsibleUsers
            )

            ButtonWithLoadingAnimation(
                modifier = Modifier
                    .heightIn(max = 400.dp)
                    .fillMaxWidth()
                    .onGloballyPositioned {
                        buttonPositionInParent = it.positionInParent().y
                        buttonHeightInParent = it.size.height
                    },
                isLoading = acceptDeferred != null,
                onClick = { acceptDeferred = onRequestAccept() }
            ) {
                Text(text = stringResource(id = R.string.code_of_conduct_button_accept))
            }

            Spacer(modifier = Modifier.height(4.dp))
        }

        if (!isButtonVisible) {
            val transition = rememberInfiniteTransition("Bouncy readme button infinite transition")
            val additionalOffsetPercent by transition.animateFloat(
                initialValue = -1f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500, easing = FastOutLinearInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "Offset Percent Transition"
            )

            Icon(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
                    .offset {
                        val offsetInDp = 4.dp.toPx()
                        IntOffset(0, (offsetInDp * additionalOffsetPercent).toInt())
                    }
                    .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                    .padding(4.dp),
                imageVector = Icons.Default.ArrowDownward,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                contentDescription = null
            )
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
