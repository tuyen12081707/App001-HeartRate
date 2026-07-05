package com.tdev.heartrate.shared.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tdev.heartrate.shared.domain.model.News
import com.tdev.heartrate.shared.domain.usecase.GetNewsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val news: List<News>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

class HomeViewModel(
    private val getNewsUseCase: GetNewsUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

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
