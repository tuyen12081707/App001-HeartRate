package com.tdev.heartrate.shared.presentation.newsdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tdev.heartrate.shared.domain.model.NewsDetail
import com.tdev.heartrate.shared.domain.usecase.GetNewsDetailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class NewsDetailUiState {
    object Loading : NewsDetailUiState()
    data class Success(val newsDetail: NewsDetail) : NewsDetailUiState()
    data class Error(val message: String) : NewsDetailUiState()
}

class NewsDetailViewModel(
    private val getNewsDetailUseCase: GetNewsDetailUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<NewsDetailUiState>(NewsDetailUiState.Loading)
    val uiState: StateFlow<NewsDetailUiState> = _uiState.asStateFlow()

    fun fetchNewsDetail(url: String) {
        _uiState.value = NewsDetailUiState.Loading
        viewModelScope.launch {
            getNewsDetailUseCase(url).collect { result ->
                result.onSuccess {
                    _uiState.value = NewsDetailUiState.Success(it)
                }.onFailure {
                    _uiState.value = NewsDetailUiState.Error(it.message ?: "Unknown error")
                }
            }
        }
    }
}
