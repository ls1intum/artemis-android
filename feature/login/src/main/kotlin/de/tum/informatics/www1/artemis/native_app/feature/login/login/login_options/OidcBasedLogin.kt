package de.tum.informatics.www1.artemis.native_app.feature.login.login.login_options

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.core.model.server_config.Saml2Config
import de.tum.informatics.www1.artemis.native_app.feature.login.R
import de.tum.informatics.www1.artemis.native_app.feature.login.login.RememberLoginCheckBox


@Composable
internal fun OidcBasedLogin(
    modifier: Modifier,
    idpName: String?,
    rememberMe: Boolean,
    updateRememberMe: (Boolean) -> Unit,
    onLoginButtonClicked: () -> Unit
) {
    val elementModifier = Modifier.fillMaxWidth()
    Column(modifier = modifier) {
        RememberLoginCheckBox(
            modifier = elementModifier,
            rememberMe = rememberMe,
            updateRememberMe = updateRememberMe
        )
        val buttonText = if (!idpName.isNullOrBlank()) {
            "Sign in with $idpName"
        } else {
            stringResource(id = R.string.login_saml_button_label)
        }
        Button(
            modifier = elementModifier,
            onClick = onLoginButtonClicked
        ) {
            Text(text = buttonText)
        }
    }
}