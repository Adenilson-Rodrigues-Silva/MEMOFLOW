package com.example.memoflow.ui.viewmodel

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
import com.example.memoflow.data.local.entity.UserEntity
import com.example.memoflow.data.repository.MemoRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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

class AuthViewModel(private val repository: MemoRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    private val auth = FirebaseAuth.getInstance()

    fun getUserSettings(): Flow<UserEntity?> = repository.userSettings

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
                // Se falhar o seletor novo, avisamos para o usuário verificar as contas
                _uiState.update { it.copy(isLoading = false, error = "Verifique se há uma conta Google ativa no seu celular.") }
            } catch (e: GetCredentialException) {
                _uiState.update { it.copy(isLoading = false, error = "Google: ${e.message}") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Erro: ${e.message}") }
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
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Erro no Firebase") }
            }
        }
    }

    fun skipSignIn() {
        viewModelScope.launch {
            val currentUser = repository.userSettings.first() ?: UserEntity()
            repository.saveUserSettings(currentUser.copy(
                isGoogleLogged = false,
                hasSeenWelcome = true
            ))
            _uiState.update { it.copy(isSuccess = true) }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as com.example.memoflow.MemoApplication
                return AuthViewModel(application.repository) as T
            }
        }
    }
}
