package com.tdev.heartrate.shared.presentation.add

import androidx.lifecycle.viewModelScope
import com.tdev.heartrate.shared.domain.model.BodyState
import com.tdev.heartrate.shared.domain.model.MeasureType
import com.tdev.heartrate.shared.domain.usecase.AddHeartRateRecordUseCase
import com.tdev.heartrate.shared.presentation.BaseViewModel
import com.tdev.heartrate.shared.presentation.DataState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddRecordUiState(
    val bpm: String = "",
    val bodyState: BodyState? = null,
    val note: String = "",
    val saveState: DataState<Long> = DataState.Idle,
    val fieldErrors: Map<String, String> = emptyMap()
) {
    val isLoading: Boolean get() = saveState is DataState.Loading
    val errorMessage: String?
        get() = (saveState as? DataState.Error)?.message ?: fieldErrors.values.firstOrNull()
}

sealed interface AddRecordIntent {
    data class UpdateBpm(val bpm: String) : AddRecordIntent
    data class UpdateBodyState(val state: BodyState) : AddRecordIntent
    data class UpdateNote(val note: String) : AddRecordIntent
    data object SaveRecord : AddRecordIntent
    data object ClearError : AddRecordIntent
}

sealed interface AddRecordSideEffect {
    data object NavigateBack : AddRecordSideEffect
    data class NavigateToResult(val recordId: Long) : AddRecordSideEffect
    data class ShowSnackbar(val message: String) : AddRecordSideEffect
}

class AddRecordViewModel(
    private val addHeartRateRecordUseCase: AddHeartRateRecordUseCase
) : BaseViewModel<AddRecordUiState, AddRecordIntent, AddRecordSideEffect>(AddRecordUiState()) {

    override fun onIntent(intent: AddRecordIntent) {
        when (intent) {
            is AddRecordIntent.UpdateBpm -> _uiState.update {
                it.copy(bpm = intent.bpm, fieldErrors = it.fieldErrors - "bpm")
            }
            is AddRecordIntent.UpdateBodyState -> _uiState.update {
                it.copy(bodyState = intent.state, fieldErrors = it.fieldErrors - "bodyState")
            }
            is AddRecordIntent.UpdateNote -> _uiState.update { it.copy(note = intent.note) }
            AddRecordIntent.ClearError -> _uiState.update {
                it.copy(saveState = DataState.Idle, fieldErrors = emptyMap())
            }
            AddRecordIntent.SaveRecord -> saveRecord()
        }
    }

    private fun saveRecord() {
        val currentState = _uiState.value
        if (currentState.saveState is DataState.Loading) return

        val bpm = currentState.bpm.toIntOrNull()
        val errors = buildMap {
            if (bpm == null) put("bpm", "Invalid BPM")
            else if (bpm !in 30..250) put("bpm", "BPM must be between 30 and 250")
            if (currentState.bodyState == null) put("bodyState", "Please select a body state")
        }
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(fieldErrors = errors, saveState = DataState.Idle) }
            return
        }

        // Set Loading before launching so a second SaveRecord intent cannot race
        // the first coroutine and insert the same form twice.
        _uiState.update { it.copy(saveState = DataState.Loading, fieldErrors = emptyMap()) }
        viewModelScope.launch {
            try {
                val id = addHeartRateRecordUseCase(
                    bpm = bpm!!,
                    measureType = MeasureType.MANUAL,
                    bodyState = currentState.bodyState!!,
                    note = currentState.note.ifBlank { null }
                )
                _uiState.update { it.copy(saveState = DataState.Success(id)) }
                emitSideEffect(AddRecordSideEffect.ShowSnackbar("Saved successfully!"))
                emitSideEffect(AddRecordSideEffect.NavigateToResult(id))
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                _uiState.update {
                    it.copy(saveState = DataState.Error(throwable.message ?: "Error saving data", throwable))
                }
            }
        }
    }
}
