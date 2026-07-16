package com.tdev.heartrate.shared.presentation.newsdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tdev.heartrate.shared.domain.model.NewsDetail
import com.tdev.heartrate.shared.domain.usecase.GetNewsDetailUseCase
import com.tdev.heartrate.shared.presentation.BaseViewModel
import com.tdev.heartrate.shared.presentation.DataState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NewsDetailUiState(
    val dataState : DataState<NewsDetail> = DataState.Loading
)

class NewsDetailViewModel(
    private val getNewsDetailUseCase: GetNewsDetailUseCase
) : BaseViewModel<NewsDetailUiState, Unit, Unit>(NewsDetailUiState()) {


    override fun onIntent(intent: Unit) {

    }

    fun fetchNewsDetail(url: String) {
        _uiState.value = _uiState.value.copy(dataState = DataState.Loading)
        viewModelScope.launch {
            getNewsDetailUseCase(url).collect { result ->
                result.onSuccess {
                    _uiState.value = _uiState.value.copy(dataState = DataState.Success(it))
                }.onFailure {
                    _uiState.value = _uiState.value.copy(dataState = DataState.Error(it.message ?: "Unknown error"))
                }
            }
        }
    }
}
