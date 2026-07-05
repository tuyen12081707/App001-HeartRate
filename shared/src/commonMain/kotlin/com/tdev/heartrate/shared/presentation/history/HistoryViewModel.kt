package com.tdev.heartrate.shared.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tdev.heartrate.shared.domain.model.HeartRateRecord
import com.tdev.heartrate.shared.domain.usecase.DeleteHeartRateRecordUseCase
import com.tdev.heartrate.shared.domain.usecase.GetHeartRateHistoryUseCase
import com.tdev.heartrate.shared.presentation.BaseViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class HistoryUiState(
    val isLoading: Boolean = true,
    val records: List<HeartRateRecord> = emptyList(),
    val isEmpty: Boolean = false
)

sealed interface HistoryIntent {
    data class DeleteRecord(val id: Long) : HistoryIntent
}

class HistoryViewModel(
    private val getHeartRateHistoryUseCase: GetHeartRateHistoryUseCase,
    private val deleteHeartRateRecordUseCase: DeleteHeartRateRecordUseCase
) : BaseViewModel<HistoryUiState, HistoryIntent, Unit>(HistoryUiState()) {

    init {
        viewModelScope.launch {
            getHeartRateHistoryUseCase()
                .map { records ->
                    HistoryUiState(
                        isLoading = false,
                        records = records,
                        isEmpty = records.isEmpty()
                    )
                }
                .collect { newState ->
                    _uiState.value = newState
                }
        }
    }

    override fun onIntent(intent: HistoryIntent) {
        when (intent) {
            is HistoryIntent.DeleteRecord -> {
                viewModelScope.launch {
                    deleteHeartRateRecordUseCase(intent.id)
                }
            }
        }
    }
}
