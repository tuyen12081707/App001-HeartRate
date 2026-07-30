package com.tdev.heartrate.shared.presentation.bloodpressure

import androidx.lifecycle.viewModelScope
import com.tdev.heartrate.shared.domain.usecase.AddBloodPressureRecordUseCase
import com.tdev.heartrate.shared.domain.utils.getCurrentTimeMillis
import com.tdev.heartrate.shared.presentation.BaseViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BloodPressureUiState(
    val systolic: Int = 122,
    val diastolic: Int = 80,
    val pulse: Int = 69,
    val timestamp: Long = getCurrentTimeMillis(),
    val note: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface BloodPressureIntent {
    data class UpdateSystolic(val value: Int) : BloodPressureIntent
    data class UpdateDiastolic(val value: Int) : BloodPressureIntent
    data class UpdatePulse(val value: Int) : BloodPressureIntent
    data class UpdateNote(val value: String) : BloodPressureIntent
    data object RefreshTimestamp : BloodPressureIntent
    data object SaveRecord : BloodPressureIntent
}

sealed interface BloodPressureSideEffect {
    data object NavigateBack : BloodPressureSideEffect
    data class ShowError(val message: String) : BloodPressureSideEffect
}

class BloodPressureViewModel(
    private val addBloodPressureRecordUseCase: AddBloodPressureRecordUseCase
) : BaseViewModel<BloodPressureUiState, BloodPressureIntent, BloodPressureSideEffect>(
    BloodPressureUiState()
) {

    override fun onIntent(intent: BloodPressureIntent) {
        when (intent) {
            is BloodPressureIntent.UpdateSystolic ->
                _uiState.update { it.copy(systolic = intent.value, errorMessage = null) }

            is BloodPressureIntent.UpdateDiastolic ->
                _uiState.update { it.copy(diastolic = intent.value, errorMessage = null) }

            is BloodPressureIntent.UpdatePulse ->
                _uiState.update { it.copy(pulse = intent.value, errorMessage = null) }

            is BloodPressureIntent.UpdateNote ->
                _uiState.update { it.copy(note = intent.value) }

            BloodPressureIntent.RefreshTimestamp ->
                _uiState.update { it.copy(timestamp = getCurrentTimeMillis()) }

            BloodPressureIntent.SaveRecord -> saveRecord()
        }
    }

    private fun saveRecord() {
        val state = _uiState.value
        val validationError = validate(state)
        if (validationError != null) {
            _uiState.update { it.copy(errorMessage = validationError) }
            emitSideEffect(BloodPressureSideEffect.ShowError(validationError))
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                addBloodPressureRecordUseCase(
                    systolic = state.systolic,
                    diastolic = state.diastolic,
                    pulse = state.pulse,
                    note = state.note.ifBlank { null }
                )
                emitSideEffect(BloodPressureSideEffect.NavigateBack)
            } catch (throwable: Throwable) {
                val message = throwable.message ?: "Unable to save blood pressure"
                _uiState.update { it.copy(errorMessage = message) }
                emitSideEffect(BloodPressureSideEffect.ShowError(message))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun validate(state: BloodPressureUiState): String? =
        when {
            state.systolic !in 40..250 -> "Systolic must be between 40 and 250"
            state.diastolic !in 20..150 -> "Diastolic must be between 20 and 150"
            state.pulse !in 30..250 -> "Pulse must be between 30 and 250"
            else -> null
        }
}
