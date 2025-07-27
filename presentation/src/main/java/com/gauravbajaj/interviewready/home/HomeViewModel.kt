package com.gauravbajaj.interviewready.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gauravbajaj.interviewready.base.UIState
import com.gauravbajaj.interviewready.data.repository.UserRepositoryImpl
import com.gauravbajaj.interviewready.model.User
import com.gauravbajaj.interviewready.usecase.GetUsersUserCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getUsersUserCase: GetUsersUserCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UIState<List<User>>>(UIState.Initial)
    val uiState: StateFlow<UIState<List<User>>> = _uiState

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = UIState.Loading
            try {
                getUsersUserCase.invoke()
                    .collect { users ->
                        _uiState.value = UIState.Success(users)
                    }
            } catch (e: Exception) {
                _uiState.value = UIState.Error(e.message ?: "Failed to load users")
            }
        }
    }
}
