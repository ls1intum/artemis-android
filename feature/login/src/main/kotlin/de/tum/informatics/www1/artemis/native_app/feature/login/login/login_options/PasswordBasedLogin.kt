package de.tum.informatics.www1.artemis.native_app.feature.login.login.login_options

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillNode
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalAutofill
import androidx.compose.ui.platform.LocalAutofillTree
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.feature.account.R
import de.tum.informatics.www1.artemis.native_app.feature.login.login.PasswordTextField
import de.tum.informatics.www1.artemis.native_app.feature.login.login.RememberLoginCheckBox

/**
 * Displays the password based login ui.
 * @param isLoggingIn if a network request to login is currently being sent/processed
 */
@Composable
internal fun PasswordBasedLogin(
    modifier: Modifier,
    username: String,
    password: String,
    rememberMe: Boolean,
    isLoginButtonEnabled: Boolean,
    isLoggingIn: Boolean,
    updateUsername: (String) -> Unit,
    updatePassword: (String) -> Unit,
    updateRememberMe: (Boolean) -> Unit,
    onClickLogin: () -> Unit,
    onClickForgotPassword: () -> Unit
) {
    val autofill = LocalAutofill.current
    val createAutofillModifier = { autofillNode: AutofillNode ->
        Modifier.onFocusChanged { focusState ->
            if (autofill != null) {
                if (focusState.isFocused) {
                    autofill.requestAutofillForNode(autofillNode)
                } else {
                    autofill.cancelAutofillForNode(autofillNode)
                }
            }
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Autofill(
            autofillTypes = listOf(AutofillType.Username),
            onFill = updateUsername
        ) { autofillNode ->
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(createAutofillModifier(autofillNode)),
                value = username,
                onValueChange = updateUsername,
                label = { Text(text = stringResource(id = R.string.login_username_label)) }
            )
        }

        Autofill(
            autofillTypes = listOf(AutofillType.Password),
            onFill = updatePassword
        ) { autofillNode ->
            PasswordTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(createAutofillModifier(autofillNode)),
                password = password,
                updatePassword = updatePassword,
                label = stringResource(id = R.string.login_password_label)
            )
        }

        RememberLoginCheckBox(
            modifier = Modifier.fillMaxWidth(),
            rememberMe = rememberMe,
            updateRememberMe = updateRememberMe
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onClickLogin,
            enabled = isLoginButtonEnabled && !isLoggingIn
        ) {
            Crossfade(targetState = isLoggingIn) { isLoggingInState ->
                if (isLoggingInState) {
                    CircularProgressIndicator()
                } else {
                    Text(text = stringResource(id = R.string.login_perform_login_button_text))
                }
            }
        }

        TextButton(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            onClick = onClickForgotPassword
        ) {
            Text(text = stringResource(id = R.string.login_password_forgot))
        }
    }
}

@ExperimentalComposeUiApi
@Composable
private fun Autofill(
    autofillTypes: List<AutofillType>,
    onFill: ((String) -> Unit),
    content: @Composable (AutofillNode) -> Unit
) {
    val autofillNode = AutofillNode(onFill = onFill, autofillTypes = autofillTypes)

    val autofillTree = LocalAutofillTree.current
    autofillTree += autofillNode

    Box(
        Modifier.onGloballyPositioned {
            autofillNode.boundingBox = it.boundsInWindow()
        }
    ) {
        content(autofillNode)
    }
}
