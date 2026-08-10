package de.mistatee.erglog.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.mistatee.erglog.data.profile.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainScreenViewModel(private val profileRepository: ProfileRepository) : ViewModel() {
    private val mutableUiState = MutableStateFlow<MainScreenUiState>(MainScreenUiState.Loading)
    val uiState: StateFlow<MainScreenUiState> = mutableUiState

    init {
        viewModelScope.launch {
            profileRepository
                .fetchProfile()
                .onSuccess { mutableUiState.value = MainScreenUiState.Success(it.username) }
                .onFailure { mutableUiState.value = MainScreenUiState.Error(it) }
        }
    }
}

sealed interface MainScreenUiState {
    object Loading : MainScreenUiState

    data class Error(val throwable: Throwable) : MainScreenUiState

    data class Success(val username: String) : MainScreenUiState
}
