package de.mistatee.erglog.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import de.mistatee.erglog.data.concept2.logbook.profile.ProfileRepository
import de.mistatee.erglog.data.concept2.logbook.results.ResultPagingSource
import de.mistatee.erglog.data.concept2.logbook.results.ResultRepository
import de.mistatee.erglog.data.concept2.logbook.results.model.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainScreenViewModel(
    private val profileRepository: ProfileRepository,
    private val resultRepository: ResultRepository,
) : ViewModel() {
    val uiState: StateFlow<MainScreenUiState>
        field = MutableStateFlow<MainScreenUiState>(MainScreenUiState.Loading)

    val resultsFlow: Flow<PagingData<Result>> =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = { ResultPagingSource(resultRepository) },
        ).flow.cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            val username = profileRepository.fetchProfile().map { it.username }
            uiState.value = username.fold(
                onSuccess = { MainScreenUiState.Success(username = it) },
                onFailure = { MainScreenUiState.Error(it) },
            )
        }
    }
}
