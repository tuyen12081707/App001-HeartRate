package com.tdev.heartrate.shared.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tdev.heartrate.shared.domain.model.News
import com.tdev.heartrate.shared.domain.usecase.GetNewsUseCase
import com.tdev.heartrate.shared.domain.usecase.GetHeartRateHistoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val news: List<News>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

class HomeViewModel(
    private val getNewsUseCase: GetNewsUseCase,
    private val getHeartRateHistoryUseCase: GetHeartRateHistoryUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val heartRateHistory: StateFlow<List<Int>> = getHeartRateHistoryUseCase()
        .map { records -> records.reversed().map { it.bpm } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        fetchNews()
    }

    fun fetchNews() {
        _uiState.value = HomeUiState.Loading
        getNewsUseCase().onEach { result ->
            result.fold(
                onSuccess = { newsList ->
                    _uiState.value = HomeUiState.Success(newsList)
                },
                onFailure = { error ->
                    _uiState.value = HomeUiState.Error(error.message ?: "Unknown Error")
                }
            )
        }.launchIn(viewModelScope)
    }
}
