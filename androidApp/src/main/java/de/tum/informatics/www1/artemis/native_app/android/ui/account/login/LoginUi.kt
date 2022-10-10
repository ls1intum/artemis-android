package de.tum.informatics.www1.artemis.native_app.android.ui.account.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(modifier: Modifier, viewModel: LoginViewModel, onLogin: () -> Unit) {
    val username by viewModel.username.collectAsState(initial = "")
    val password by viewModel.password.collectAsState(initial = "")
    val rememberMe by viewModel.rememberMe.collectAsState(initial = false)
    val isLoginEnabled by viewModel.loginButtonEnabled.collectAsState(initial = false)

    Scaffold(modifier = modifier) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Welcome to Artemis!",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    text = "Please login with your TUM login credentials.",
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center
                )

                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val elementModifier = Modifier
                        .widthIn(max = 600.dp)
                        .fillMaxWidth(0.8f)

                    TextField(
                        modifier = elementModifier,
                        value = username,
                        onValueChange = viewModel::updateUsername,
                        label = { Text("Login") }
                    )

                    TextField(
                        modifier = elementModifier,
                        value = password,
                        onValueChange = viewModel::updatePassword,
                        label = { Text("Password") },
                        visualTransformation = remember { PasswordVisualTransformation() }
                    )

                    Row(modifier = elementModifier) {
                        Checkbox(
                            modifier = Modifier,
                            checked = rememberMe,
                            onCheckedChange = viewModel::updateRememberMe,
                        )

                        Text(
                            modifier = Modifier
                                .weight(1f)
                                .align(Alignment.CenterVertically),
                            text = "Automatic login"
                        )
                    }

                    Button(
                        modifier = elementModifier,
                        onClick = {
                            viewModel.login(onSuccess = onLogin, onFailure = {
                                //TODO: display an error dialog.
                            }
                            )
                        },
                        enabled = isLoginEnabled
                    ) {
                        Text(text = "Login")
                    }

                }
            }
        }
    }
}