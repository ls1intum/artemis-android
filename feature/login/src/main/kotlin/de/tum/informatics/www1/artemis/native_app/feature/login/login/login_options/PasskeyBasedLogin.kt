package de.tum.informatics.www1.artemis.native_app.feature.login.login.login_options

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.core.ui.common.ButtonWithLoadingAnimation
import de.tum.informatics.www1.artemis.native_app.feature.login.R

@Composable
fun PasskeyBasedLogin(
    modifier: Modifier = Modifier,
    onPasskeyLogin: () -> Unit,
    isEnabled: Boolean = true,
    isLoggingIn: Boolean = false,
) {
    ButtonWithLoadingAnimation(
        modifier = modifier,
        onClick = onPasskeyLogin,
        enabled = isEnabled,
        isLoading = isLoggingIn,
    ) {
        Text(text = stringResource(id = R.string.login_passkey_button_label))
    }
}