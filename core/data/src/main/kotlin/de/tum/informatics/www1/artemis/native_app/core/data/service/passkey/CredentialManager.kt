package de.tum.informatics.www1.artemis.native_app.core.data.service.passkey

import android.content.Context
import android.util.Log
import androidx.credentials.CreateCredentialResponse
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.PasswordCredential
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.CreateCredentialCancellationException
import androidx.credentials.exceptions.CreateCredentialCustomException
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.CreateCredentialInterruptedException
import androidx.credentials.exceptions.CreateCredentialProviderConfigurationException
import androidx.credentials.exceptions.CreateCredentialUnknownException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.domerrors.DomError
import androidx.credentials.exceptions.publickeycredential.CreatePublicKeyCredentialDomException

// From: https://developer.android.com/identity/sign-in/credential-manager#kotlin

private const val TAG = "CredentialManager"

class CredentialManager(
    private val context: Context,
) {

    private val credentialManager = CredentialManager.create(context)

    suspend fun createPasskey(requestJson: String, preferImmediatelyAvailableCredentials: Boolean) {
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
            handlePasskeyRegistrationResult(result)
        } catch (e : CreateCredentialException){
            handlePassKeyCreationFailure(e)
        }
    }

    private fun handlePasskeyRegistrationResult(result: CreateCredentialResponse) {
        TODO("Not yet implemented")
    }

    private fun handlePassKeyCreationFailure(e: CreateCredentialException) {
        when (e) {
            is CreatePublicKeyCredentialDomException -> {
                // Handle the passkey DOM errors thrown according to the
                // WebAuthn spec.
                handlePasskeyError(e.domError)
            }
            is CreateCredentialCancellationException -> {
                // The user intentionally canceled the operation and chose not
                // to register the credential.
            }
            is CreateCredentialInterruptedException -> {
                // Retry-able error. Consider retrying the call.
            }
            is CreateCredentialProviderConfigurationException -> {
                // Your app is missing the provider configuration dependency.
                // Most likely, you're missing the
                // "credentials-play-services-auth" module.
            }
            is CreateCredentialUnknownException -> {

            }
            is CreateCredentialCustomException -> {
                // You have encountered an error from a 3rd-party SDK. If you
                // make the API call with a request object that's a subclass of
                // CreateCustomCredentialRequest using a 3rd-party SDK, then you
                // should check for any custom exception type constants within
                // that SDK to match with e.type. Otherwise, drop or log the
                // exception.
            }
            else -> Log.w(TAG, "Unexpected exception type ${e::class.java.name}")
        }
    }

    private fun handlePasskeyError(domError: DomError) {
        TODO("Not yet implemented")
    }

    suspend fun signInWithPasskey(requestJson: String) {
        val getPublicKeyCredentialOption = GetPublicKeyCredentialOption(
            requestJson = requestJson
        )
        val getCredRequest = GetCredentialRequest(
            listOf(getPublicKeyCredentialOption)
        )

        try {
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

    private fun handleSignIn(result: GetCredentialResponse) {
        // Handle the successfully returned credential.
        val credential = result.credential

        when (credential) {
            is PublicKeyCredential -> {
                val responseJson = credential.authenticationResponseJson
                // Share responseJson i.e. a GetCredentialResponse on your server to
                // validate and  authenticate
            }
            is PasswordCredential -> {
                val username = credential.id
                val password = credential.password
                // Use id and password to send to your server to validate
                // and authenticate
            } else -> {
                // Catch any unrecognized credential type here.
            Log.e(TAG, "Unexpected type of credential")
            }
        }
    }

    private fun handleSignInFailure(exception: GetCredentialException) {
        TODO("Not yet implemented")
    }

}