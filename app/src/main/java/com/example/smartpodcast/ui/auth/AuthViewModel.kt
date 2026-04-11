package com.example.smartpodcast.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartpodcast.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: FirebaseUser) : AuthState()
    object RegisterSuccess : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(email: String, pass: String) {
        if (!validate(email, pass)) return
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.login(email, pass)
            result.onSuccess { user ->
                _authState.value = AuthState.Success(user)
            }.onFailure { ex ->
                _authState.value = AuthState.Error(ex.message ?: "Lỗi Đăng Nhập")
            }
        }
    }

    fun register(email: String, pass: String) {
        if (!validate(email, pass)) return
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.register(email, pass)
            result.onSuccess { user ->
                authRepository.logout() // Đăng xuất ngay sau khi đăng ký để bắt buộc đăng nhập thủ công
                _authState.value = AuthState.RegisterSuccess
            }.onFailure { ex ->
                _authState.value = AuthState.Error(ex.message ?: "Lỗi Đăng Ký")
            }
        }
    }

    private fun validate(email: String, pass: String): Boolean {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("Email và mật khẩu không được rỗng!")
            return false
        }
        if (pass.length < 6) {
            _authState.value = AuthState.Error("Mật khẩu phải có ít nhất 6 ký tự!")
            return false
        }
        return true
    }
}
