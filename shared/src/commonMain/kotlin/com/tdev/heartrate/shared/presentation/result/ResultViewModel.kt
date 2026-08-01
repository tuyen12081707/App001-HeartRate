package com.tdev.heartrate.shared.presentation.result

import androidx.lifecycle.viewModelScope
import com.tdev.heartrate.shared.domain.model.HeartRateRecord
import com.tdev.heartrate.shared.domain.usecase.GetHeartRateRecordUseCase
import com.tdev.heartrate.shared.presentation.BaseViewModel
import com.tdev.heartrate.shared.presentation.DataState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

data class ResultUiState(
    val data: DataState<HeartRateRecord> = DataState.Idle
)

class ResultViewModel(
    private val getHeartRateRecordUseCase: GetHeartRateRecordUseCase,
    private val recordId: Long
) : BaseViewModel<ResultUiState, Unit, Unit>(ResultUiState(DataState.Loading)) {

    init {
        viewModelScope.launch {
            try {
                val record = getHeartRateRecordUseCase(recordId)
                _uiState.value = ResultUiState(
                    if (record == null) DataState.Error("Heart rate record not found")
                    else DataState.Success(record)
                )
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                _uiState.value = ResultUiState(
                    DataState.Error(throwable.message ?: "Unable to load result", throwable)
                )
            }
        }
    }

    override fun onIntent(intent: Unit) = Unit
}
