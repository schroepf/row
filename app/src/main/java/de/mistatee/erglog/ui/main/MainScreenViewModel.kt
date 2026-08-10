package de.mistatee.erglog.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.mistatee.erglog.data.profile.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainScreenViewModel(private val profileRepository: ProfileRepository) : ViewModel() {
    val uiState: StateFlow<MainScreenUiState>
        field = MutableStateFlow<MainScreenUiState>(MainScreenUiState.Loading)

    init {
        viewModelScope.launch {
            profileRepository
                .fetchProfile()
                .onSuccess { uiState.value = MainScreenUiState.Success(it.username) }
                .onFailure { uiState.value = MainScreenUiState.Error(it) }
        }
    }
}

