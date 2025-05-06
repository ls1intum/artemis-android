package de.tum.informatics.www1.artemis.native_app.core.data.service.passkey

import android.content.Context
import android.util.Log
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.CreatePublicKeyCredentialResponse
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.CreateCredentialCancellationException
import androidx.credentials.exceptions.CreateCredentialCustomException
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.CreateCredentialInterruptedException
import androidx.credentials.exceptions.CreateCredentialProviderConfigurationException
import androidx.credentials.exceptions.CreateCredentialUnknownException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.credentials.exceptions.publickeycredential.CreatePublicKeyCredentialDomException

// From: https://developer.android.com/identity/sign-in/credential-manager#kotlin

private const val TAG = "LocalPasskeyManager"

class CredentialManagerWrapper(
    private val context: Context,
) {

    private val credentialManager = CredentialManager.create(context)

    sealed class PasskeyCreationResult {
        data class Success(val response: CreatePublicKeyCredentialResponse) : PasskeyCreationResult()
        data object Canceled : PasskeyCreationResult()
        data class Failure(val error: Exception) : PasskeyCreationResult()
    }

    suspend fun createPasskey(
        requestJson: String,
        preferImmediatelyAvailableCredentials: Boolean = false,
    ): PasskeyCreationResult {
        val createPublicKeyCredentialRequest = CreatePublicKeyCredentialRequest(
            // Contains the request in JSON format. Uses the standard WebAuthn
            // web JSON spec.
            requestJson = requestJson,
            // Defines whether you prefer to use only immediately available
            // credentials, not hybrid credentials, to fulfill this request.
            // This value is false by default.
            preferImmediatelyAvailableCredentials = preferImmediatelyAvailableCredentials,
        )

        // Execute CreateCredentialRequest asynchronously to register credentials
        // for a user account. Handle success and failure cases with the result and
        // exceptions, respectively.
        try {
            val result = credentialManager.createCredential(
                // Use an activity-based context to avoid undefined system
                // UI launching behavior
                context = context,
                request = createPublicKeyCredentialRequest,
            )
            return PasskeyCreationResult.Success(result as CreatePublicKeyCredentialResponse)
        } catch (e : CreateCredentialException){
            return handlePassKeyCreationFailure(
                error = e,
                onRetry = {
                    // Retry the request if needed.
                    createPasskey(
                        requestJson = requestJson,
                        preferImmediatelyAvailableCredentials = preferImmediatelyAvailableCredentials
                    )
                }
            )
        }
    }

    private suspend fun handlePassKeyCreationFailure(
        error: CreateCredentialException,
        onRetry: suspend () -> PasskeyCreationResult,
    ): PasskeyCreationResult {
        when (error) {
            is CreatePublicKeyCredentialDomException -> {
                // Handle the passkey DOM errors thrown according to the
                // WebAuthn spec.
                // TODO: we always get a "Passkey creation failed with DOM error: androidx.credentials.TYPE_SECURITY_ERROR: The incoming request cannot be validated"
                //      With GPM: "RP ID cannot be validated"
                Log.e(TAG, "Passkey creation failed with DOM error: ${error.domError.type}: ${error.message}")
                return PasskeyCreationResult.Failure(error)
            }
            is CreateCredentialCancellationException -> {
                // The user intentionally canceled the operation and chose not
                // to register the credential.
                Log.d(TAG, "Passkey creation canceled by user")
                return PasskeyCreationResult.Canceled
            }
            is CreateCredentialInterruptedException -> {
                // Retry-able error. Consider retrying the call.
                return onRetry()
            }
            is CreateCredentialProviderConfigurationException -> {
                // Your app is missing the provider configuration dependency.
                // Most likely, you're missing the
                // "credentials-play-services-auth" module.
                Log.e(TAG, "Passkey creation failed with provider configuration error: ${error.message}")
                return PasskeyCreationResult.Failure(error)
            }
            is CreateCredentialUnknownException -> {
                Log.e(TAG, "Passkey creation failed with unknown error: ${error.message}")
                return PasskeyCreationResult.Failure(error)
            }
            is CreateCredentialCustomException -> {
                // You have encountered an error from a 3rd-party SDK. If you
                // make the API call with a request object that's a subclass of
                // CreateCustomCredentialRequest using a 3rd-party SDK, then you
                // should check for any custom exception type constants within
                // that SDK to match with e.type. Otherwise, drop or log the
                // exception.
                Log.e(TAG, "Passkey creation failed with custom error: ${error.message}")
                return PasskeyCreationResult.Failure(error)
            }
            else -> {
                Log.w(TAG, "Unexpected exception type ${error::class.java.name}")
                return PasskeyCreationResult.Failure(error)
            }
        }
    }

    sealed class SignInResult {
        data class WithPasskey(val responseJson: String) : SignInResult()
        data object NoCredential : SignInResult()
        data class Failure(val error: Exception) : SignInResult()
    }

    suspend fun signIn(requestJson: String): SignInResult {
        val getPublicKeyCredentialOption = GetPublicKeyCredentialOption(
            requestJson = requestJson
        )
        val getCredRequest = GetCredentialRequest(
            listOf(getPublicKeyCredentialOption)
        )

        return try {
            val result = credentialManager.getCredential(
                // Use an activity-based context to avoid undefined system UI
                // launching behavior.
                context = context,
                request = getCredRequest
            )
            handleSignIn(result)
        } catch (e: GetCredentialException) {
            handleSignInFailure(e)
        }
    }

    private fun handleSignIn(result: GetCredentialResponse): SignInResult {
        // Handle the successfully returned credential.
        val credential = result.credential

        return when (credential) {
            is PublicKeyCredential -> {
                val responseJson = credential.authenticationResponseJson
                // Return the response JSON for server validation and authentication.
                SignInResult.WithPasskey(responseJson)
            }
            else -> {
                // Catch any unrecognized credential type here.
                Log.e(TAG, "Unexpected type of credential")
                SignInResult.Failure(Exception("Unexpected credential type"))
            }
        }
    }

    private fun handleSignInFailure(exception: GetCredentialException): SignInResult {
        return when (exception) {
            is NoCredentialException -> {
                // No credentials were found. The user may not have registered
                // any credentials yet.
                Log.d(TAG, "No credentials found")
                SignInResult.NoCredential
            }
            else -> {
                // Handle other errors.
                Log.e(TAG, "Sign-in failed with error: ${exception.type}: ${exception.message}")
                SignInResult.Failure(exception)
            }
        }
    }

}