package de.tum.informatics.www1.artemis.native_app.feature.login.service

/**
 * This interface is used to wrap around the Android CredentialManager API.
 * See: https://developer.android.com/identity/sign-in/credential-manager#kotlin
 */
interface AndroidCredentialService {

    suspend fun createPasskey(
        requestJson: String,
        preferImmediatelyAvailableCredentials: Boolean = false,
    ): PasskeyCreationResult

    suspend fun signIn(requestJson: String): SignInResult

    sealed class PasskeyCreationResult {
        data class Success(val registrationResponseJson: String) : PasskeyCreationResult()
        data object Cancelled : PasskeyCreationResult()
        data class Failure(val error: Exception) : PasskeyCreationResult()
    }

    sealed class SignInResult {
        data class WithPasskey(val responseJson: String) : SignInResult()
        data object NoCredential : SignInResult()
        data class Failure(val error: Exception) : SignInResult()
    }
}