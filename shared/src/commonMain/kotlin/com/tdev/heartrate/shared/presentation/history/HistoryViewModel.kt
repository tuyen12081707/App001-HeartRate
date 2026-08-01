package com.tdev.heartrate.shared.presentation.history

import androidx.lifecycle.viewModelScope
import com.tdev.heartrate.shared.domain.model.HeartRateRecord
import com.tdev.heartrate.shared.domain.usecase.DeleteHeartRateRecordUseCase
import com.tdev.heartrate.shared.domain.usecase.GetHeartRateHistoryUseCase
import com.tdev.heartrate.shared.presentation.BaseViewModel
import com.tdev.heartrate.shared.presentation.DataState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistoryUiState(
    val data: DataState<List<HeartRateRecord>> = DataState.Idle,
    val deleteState: DataState<Long> = DataState.Idle
) {
    val isLoading: Boolean get() = data is DataState.Loading || data is DataState.Idle
    val records: List<HeartRateRecord> get() = (data as? DataState.Success)?.data.orEmpty()
    val isEmpty: Boolean get() = data is DataState.Success && records.isEmpty()
}

sealed interface HistoryIntent {
    data class DeleteRecord(val id: Long) : HistoryIntent
}

class HistoryViewModel(
    private val getHeartRateHistoryUseCase: GetHeartRateHistoryUseCase,
    private val deleteHeartRateRecordUseCase: DeleteHeartRateRecordUseCase
) : BaseViewModel<HistoryUiState, HistoryIntent, Unit>(HistoryUiState()) {

    init {
        _uiState.value = HistoryUiState(data = DataState.Loading)
        viewModelScope.launch {
            getHeartRateHistoryUseCase()
                .map<List<HeartRateRecord>, DataState<List<HeartRateRecord>>> { DataState.Success(it) }
                .catch { throwable ->
                    if (throwable is CancellationException) throw throwable
                    emit(DataState.Error(throwable.message ?: "Unable to load history", throwable))
                }
                .collect { state -> _uiState.update { it.copy(data = state) } }
        }
    }

    override fun onIntent(intent: HistoryIntent) {
        when (intent) {
            is HistoryIntent.DeleteRecord -> delete(intent.id)
        }
    }

    private fun delete(id: Long) {
        val current = _uiState.value
        if (current.deleteState is DataState.Loading) return
        viewModelScope.launch {
            _uiState.update { it.copy(deleteState = DataState.Loading) }
            try {
                deleteHeartRateRecordUseCase(id)
                _uiState.update { it.copy(deleteState = DataState.Success(id)) }
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                _uiState.update {
                    it.copy(deleteState = DataState.Error(throwable.message ?: "Unable to delete record", throwable))
                }
            }
        }
    }
}
