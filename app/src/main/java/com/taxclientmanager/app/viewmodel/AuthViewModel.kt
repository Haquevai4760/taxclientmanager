package com.taxclientmanager.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taxclientmanager.app.data.repository.AuthRepository
import com.taxclientmanager.app.utils.NetworkUtils
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val settings: Settings
) : ViewModel() {

    val currentUser: StateFlow<UserInfo?> = authRepository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<AuthNavigation>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    val lastUsedEmail: String
        get() = settings.getString("last_email", "")

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                authRepository.login(email, password)
                // Save email for next time
                settings["last_email"] = email
                _authState.value = AuthState.Success
                _navigationEvent.emit(AuthNavigation.ToDashboard)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(NetworkUtils.getReadableError(e))
            }
        }
    }

    fun signup(fullName: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                authRepository.signup(email, password, fullName)
                _authState.value = AuthState.Success
                _navigationEvent.emit(AuthNavigation.ToLogin)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(NetworkUtils.getReadableError(e))
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun updateProfile(name: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                authRepository.updateProfile(name)
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(NetworkUtils.getReadableError(e))
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                authRepository.changePassword(currentPassword, newPassword)
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(NetworkUtils.getReadableError(e))
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                authRepository.sendResetLink(email)
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(NetworkUtils.getReadableError(e))
            }
        }
    }

    fun updatePassword(password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                authRepository.updatePassword(password)
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(NetworkUtils.getReadableError(e))
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

sealed class AuthNavigation {
    object ToDashboard : AuthNavigation()
    object ToLogin : AuthNavigation()
}
