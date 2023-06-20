package de.tum.informatics.www1.artemis.native_app.feature.login.login.login_options

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import de.tum.informatics.www1.artemis.native_app.core.model.server_config.Saml2Config
import de.tum.informatics.www1.artemis.native_app.feature.login.R
import de.tum.informatics.www1.artemis.native_app.feature.login.login.RememberLoginCheckBox

@Composable
internal fun Saml2BasedLogin(
    modifier: Modifier,
    saml2Config: Saml2Config,
    passwordLoginDisabled: Boolean,
    needsToAcceptTerms: Boolean,
    hasUserAcceptedTerms: Boolean,
    rememberMe: Boolean,
    updateRememberMe: (Boolean) -> Unit,
    onLoginButtonClicked: () -> Unit
) {
    val elementModifier = Modifier.fillMaxWidth()

    Column(modifier = modifier) {
        val identityProviderName = saml2Config.identityProviderName
        val pleaseSignInText = if (identityProviderName != null) {
            stringResource(
                id = R.string.login_saml_please_sign_in_provider,
                identityProviderName
            )
        } else stringResource(id = R.string.login_saml_please_sign_in)

        if (!passwordLoginDisabled) {
            Text(
                modifier = elementModifier,
                text = pleaseSignInText,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
        }

        RememberLoginCheckBox(
            modifier = elementModifier,
            rememberMe = rememberMe,
            updateRememberMe = updateRememberMe
        )

        Button(
            modifier = elementModifier,
            onClick = onLoginButtonClicked,
            enabled = !needsToAcceptTerms || hasUserAcceptedTerms,
            content = {
                Text(
                    text = saml2Config.buttonLabel
                        ?: stringResource(id = R.string.login_saml_button_label)
                )
            }
        )

        if (needsToAcceptTerms && !hasUserAcceptedTerms) {
            Text(
                modifier = elementModifier,
                text = stringResource(id = R.string.login_error_accept_terms),
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
