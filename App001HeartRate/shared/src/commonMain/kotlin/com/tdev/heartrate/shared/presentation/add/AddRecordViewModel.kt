package com.tdev.heartrate.shared.presentation.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tdev.heartrate.shared.domain.model.BodyState
import com.tdev.heartrate.shared.domain.model.MeasureType
import com.tdev.heartrate.shared.domain.usecase.AddHeartRateRecordUseCase
import com.tdev.heartrate.shared.presentation.BaseViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddRecordUiState(
    val bpm: String = "",
    val bodyState: BodyState? = null,
    val note: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface AddRecordIntent {
    data class UpdateBpm(val bpm: String) : AddRecordIntent
    data class UpdateBodyState(val state: BodyState) : AddRecordIntent
    data class UpdateNote(val note: String) : AddRecordIntent
    object SaveRecord : AddRecordIntent
    object ClearError : AddRecordIntent
}

sealed interface AddRecordSideEffect {
    object NavigateBack : AddRecordSideEffect
    data class NavigateToResult(val bpm: Int) : AddRecordSideEffect
    data class ShowSnackbar(val message: String) : AddRecordSideEffect
}

class AddRecordViewModel(
    private val addHeartRateRecordUseCase: AddHeartRateRecordUseCase
) : BaseViewModel<AddRecordUiState, AddRecordIntent, AddRecordSideEffect>(AddRecordUiState()) {

    override fun onIntent(intent: AddRecordIntent) {
        when (intent) {
            is AddRecordIntent.UpdateBpm -> {
                _uiState.update { it.copy(bpm = intent.bpm, errorMessage = null) }
            }
            is AddRecordIntent.UpdateBodyState -> {
                _uiState.update { it.copy(bodyState = intent.state) }
            }
            is AddRecordIntent.UpdateNote -> {
                _uiState.update { it.copy(note = intent.note) }
            }
            is AddRecordIntent.ClearError -> {
                _uiState.update { it.copy(errorMessage = null) }
            }
            AddRecordIntent.SaveRecord -> saveRecord()
        }
    }

    private fun saveRecord() {
        val currentState = _uiState.value
        val bpmInt = currentState.bpm.toIntOrNull()

        if (bpmInt == null) {
            _uiState.update { it.copy(errorMessage = "Invalid BPM") }
            return
        }

        if (bpmInt !in 30..250) {
            _uiState.update { it.copy(errorMessage = "BPM must be between 30 and 250") }
            return
        }

        if (currentState.bodyState == null) {
            _uiState.update { it.copy(errorMessage = "Please select a body state") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                addHeartRateRecordUseCase(
                    bpm = bpmInt,
                    measureType = MeasureType.MANUAL,
                    bodyState = currentState.bodyState,
                    note = currentState.note.ifBlank { null }
                )
                emitSideEffect(AddRecordSideEffect.ShowSnackbar("Saved successfully!"))
                emitSideEffect(AddRecordSideEffect.NavigateToResult(bpmInt))
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Error saving data") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
