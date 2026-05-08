package com.arsdevstudio.memoflow.ui.viewmodel

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.arsdevstudio.memoflow.data.local.entity.UserEntity
import com.arsdevstudio.memoflow.data.repository.MemoRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

sealed class AuthEvent {
    object LogoutSuccess : AuthEvent()
    object LoginSuccess : AuthEvent()
}

class AuthViewModel(
    private val repository: MemoRepository,
    private val billingPrefs: com.arsdevstudio.memoflow.utils.BillingPrefs,
    private val billingManager: com.arsdevstudio.memoflow.utils.BillingManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AuthEvent>()
    val events = _events.asSharedFlow()

    private val auth = FirebaseAuth.getInstance()

    fun getUserSettings(): Flow<UserEntity?> = repository.userSettings

    fun updateHasSeenWelcome(hasSeen: Boolean) {
        viewModelScope.launch {
            val currentUser = repository.userSettings.first() ?: UserEntity()
            repository.saveUserSettings(currentUser.copy(hasSeenWelcome = hasSeen))
        }
    }

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val credentialManager = CredentialManager.create(context)
                
                val rawNonce = UUID.randomUUID().toString()
                val bytes = rawNonce.toByteArray()
                val md = MessageDigest.getInstance("SHA-256")
                val digest = md.digest(bytes)
                val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

                // Mudança Crítica: Permitir que o Google procure por contas mesmo sem autoseleção
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false) // ✅ Mostra todas as contas do celular
                    .setServerClientId("376900869050-17pdo2u3nuoq5f9ojvd22qni221emhu1.apps.googleusercontent.com")
                    .setAutoSelectEnabled(false)
                    .setNonce(hashedNonce)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(context, request)
                handleSignInResult(result)
                
            } catch (e: NoCredentialException) {
                _uiState.update { it.copy(isLoading = false, error = "Erro: Conta não encontrada. Verifique se o SHA-1 (Debug e Play Store) está no Firebase.") }
            } catch (e: GetCredentialException) {
                _uiState.update { it.copy(isLoading = false, error = "Erro Google: ${e.message} (${e.type})") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Erro inesperado: ${e.message}") }
            }
        }
    }

    private suspend fun handleSignInResult(result: GetCredentialResponse) {
        val credential = result.credential
        
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
            
            try {
                val authResult = auth.signInWithCredential(firebaseCredential).await()
                val firebaseUser = authResult.user
                
                if (firebaseUser != null) {
                    val currentUser = repository.userSettings.first() ?: UserEntity()
                    
                    val updatedUser = currentUser.copy(
                        userName = firebaseUser.displayName ?: currentUser.userName,
                        email = firebaseUser.email,
                        profilePhotoUri = firebaseUser.photoUrl?.toString() ?: currentUser.profilePhotoUri,
                        firebaseUid = firebaseUser.uid,
                        isGoogleLogged = true,
                        hasSeenWelcome = true
                    )
                    
                    repository.saveUserSettings(updatedUser)
                    billingManager.queryPurchases() // Atualiza o status premium para a nova conta
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                    _events.emit(AuthEvent.LoginSuccess)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Erro no Firebase") }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            auth.signOut()
            val currentUser = repository.userSettings.first() ?: UserEntity()
            repository.saveUserSettings(currentUser.copy(
                isGoogleLogged = false,
                email = null,
                firebaseUid = null,
                userName = "",
                bio = "",
                profilePhotoUri = null,
                pin = null, // Limpa o PIN por segurança
                isBiometricEnabled = false, // Desativa biometria para o novo usuário
                recallCount = 0,
                gratitudeRecallCount = 0,
                hasSeenWelcome = false // Força a volta para a tela de Boas-Vindas no próximo boot
            ))
            billingPrefs.setPremium(false) // Resetar para forçar re-validação pelo BillingManager
            _uiState.update { it.copy(isSuccess = false, error = null) }
            _events.emit(AuthEvent.LogoutSuccess)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as com.arsdevstudio.memoflow.MemoApplication
                return AuthViewModel(application.repository, application.billingPrefs, application.billingManager) as T
            }
        }
    }
}

