package com.schroepf.row

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class RowViewModel(
    private val rowApi: RowApi
) : ViewModel() {
    private val _state = MutableStateFlow(RowState())
    val state: StateFlow<RowState> = _state.asStateFlow()

    init {
        send(RowIntent.Refresh)
    }

    fun send(intent: RowIntent) {
        when (intent) {
            RowIntent.Refresh -> refresh()
        }
    }

    private fun refresh() {
        _state.update { RowReducer.reduce(it, RowResult.Loading) }
        viewModelScope.launch {
            runCatching {
                rowApi.fetchWelcomeMessage()
            }.onSuccess { message ->
                _state.update { RowReducer.reduce(it, RowResult.Success(message)) }
            }.onFailure { throwable ->
                _state.update {
                    RowReducer.reduce(
                        it,
                        RowResult.Failure(throwable.message ?: "Unexpected error")
                    )
                }
            }
        }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val client = HttpClient(CIO) {
                        install(ContentNegotiation) {
                            json(Json { ignoreUnknownKeys = true })
                        }
                    }
                    @Suppress("UNCHECKED_CAST")
                    return RowViewModel(KtorRowApi(client)) as T
                }
            }
    }
}
