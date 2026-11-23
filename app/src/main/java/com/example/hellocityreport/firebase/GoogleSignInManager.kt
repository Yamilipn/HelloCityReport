package com.example.hellocityreport.firebase

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.NoCredentialException
import com.example.hellocityreport.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

/**
 * Autenticación con Google usando Credential Manager + Firebase Auth.
 *
 * Flujo: Credential Manager -> Google ID token -> GoogleAuthProvider ->
 * FirebaseAuth.signInWithCredential -> FirebaseUser.
 *
 * Requiere:
 *  - proveedor Google habilitado en Firebase Console;
 *  - SHA-1 de la app registrado en Firebase Console;
 *  - google-services.json con oauth_client de client_type 3, que genera
 *    R.string.default_web_client_id (el client ID WEB, no el de Android).
 */
class GoogleSignInManager(private val context: Context) {

    private val credentialManager: CredentialManager = CredentialManager.create(context)
    private val auth = FirebaseModule.auth

    private fun buildRequest(filterByAuthorized: Boolean, autoSelect: Boolean): GetCredentialRequest {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(context.getString(R.string.default_web_client_id))
            .setFilterByAuthorizedAccounts(filterByAuthorized)
            .setAutoSelectEnabled(autoSelect)
            .build()

        return GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
    }

    /**
     * Inicia sesión y devuelve el FirebaseUser resultante.
     * Usar desde un coroutine (por ejemplo viewModelScope.launch).
     */
    suspend fun signIn(): Result<FirebaseUser?> = runCatching {
        val idToken = requestGoogleIdToken()
        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(firebaseCredential).await().user
    }

    private suspend fun requestGoogleIdToken(): String {
        // Primero intentamos con cuentas ya autorizadas (inicio silencioso).
        val response: GetCredentialResponse = try {
            credentialManager.getCredential(context, buildRequest(true, true))
        } catch (_: NoCredentialException) {
            // No hay cuentas previas: mostramos todas las disponibles.
            credentialManager.getCredential(context, buildRequest(false, false))
        }
        return extractIdToken(response.credential)
    }

    private fun extractIdToken(credential: Credential): String {
        require(
            credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) { "La credencial recibida no es de tipo Google ID" }

        return GoogleIdTokenCredential.createFrom(credential.data).idToken
    }

    fun currentUser(): FirebaseUser? = auth.currentUser

    /** Cierra sesión en Firebase y limpia la cuenta recordada por Credential Manager. */
    suspend fun signOut() {
        auth.signOut()
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }
}
