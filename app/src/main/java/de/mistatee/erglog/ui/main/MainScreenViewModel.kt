package de.mistatee.erglog.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import de.mistatee.erglog.data.concept2.logbook.profile.ProfileRepository
import de.mistatee.erglog.data.concept2.logbook.results.ResultRepository
import de.mistatee.erglog.data.concept2.logbook.results.model.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Room is the source of truth for both Profile and Result History (see
 * docs/adr/0005-room-for-offline-first-persistence.md): [uiState] and [resultsFlow] both observe
 * the cache directly. Refresh-on-launch happens once per ViewModel lifetime for Profile (via
 * [refresh], called from [init]) and once per [resultsFlow] Pager lifetime for Result History (via
 * [de.mistatee.erglog.data.local.results.ResultRemoteMediator.initialize]); pull-to-refresh in the
 * UI re-triggers both by calling [refresh] and the paging items' own `refresh()`.
 *
 * A failed Profile refresh does not blank out an already-cached username — it's surfaced via
 * [MainScreenUiState.Success.syncError] instead, since Stale (see CONTEXT.md) cached data is still
 * worth showing. A failed Result History sync is surfaced separately by the UI observing
 * `resultsFlow`'s own `LoadState.Error`, since [ResultRepository] exposes `PagingData` rather than
 * a `Result<...>` this ViewModel could inspect directly.
 */
class MainScreenViewModel(
    private val profileRepository: ProfileRepository,
    private val resultRepository: ResultRepository,
) : ViewModel() {
    private val syncError = MutableStateFlow<Throwable?>(null)

    val uiState: StateFlow<MainScreenUiState>
        field = MutableStateFlow<MainScreenUiState>(MainScreenUiState.Loading)

    val isRefreshing: StateFlow<Boolean>
        field = MutableStateFlow(false)

    val resultsFlow: Flow<PagingData<Result>> = resultRepository.observeResults().cachedIn(viewModelScope)

    init {
        combine(profileRepository.observeProfile(), syncError) { profile, error ->
            when {
                profile != null -> MainScreenUiState.Success(username = profile.username, syncError = error)
                error != null -> MainScreenUiState.Error(error)
                else -> MainScreenUiState.Loading
            }
        }.onEach { uiState.value = it }.launchIn(viewModelScope)

        refresh()
    }

    /**
     * Re-triggers a Profile refresh from Concept2, e.g. in response to a pull-to-refresh gesture.
     * Result History is refreshed separately by the UI calling the paging items' own `refresh()`.
     */
    fun refresh() {
        viewModelScope.launch {
            isRefreshing.value = true
            profileRepository.refresh()
                .onSuccess { syncError.value = null }
                .onFailure { syncError.value = it }
            isRefreshing.value = false
        }
    }
}
