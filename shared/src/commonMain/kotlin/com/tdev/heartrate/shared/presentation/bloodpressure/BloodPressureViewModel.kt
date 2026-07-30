package com.tdev.heartrate.shared.presentation.bloodpressure

import androidx.lifecycle.viewModelScope
import com.tdev.heartrate.shared.domain.model.BloodPressureLevel
import com.tdev.heartrate.shared.domain.model.BloodPressureInputConstraints
import com.tdev.heartrate.shared.domain.model.classifyBloodPressure
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
) {
    val level: BloodPressureLevel
        get() = classifyBloodPressure(systolic = systolic, diastolic = diastolic)
}

sealed interface BloodPressureIntent {
    data class UpdateSystolic(val value: Int) : BloodPressureIntent
    data class UpdateDiastolic(val value: Int) : BloodPressureIntent
    data class UpdatePulse(val value: Int) : BloodPressureIntent
    data class UpdateTimestamp(val value: Long) : BloodPressureIntent
    data class UpdateNote(val value: String) : BloodPressureIntent
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

            is BloodPressureIntent.UpdateTimestamp ->
                _uiState.update { it.copy(timestamp = intent.value, errorMessage = null) }

            is BloodPressureIntent.UpdateNote ->
                _uiState.update { it.copy(note = intent.value) }

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
                    timestamp = state.timestamp,
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
            state.systolic !in BloodPressureInputConstraints.SYSTOLIC_RANGE ->
                "Systolic must be between ${BloodPressureInputConstraints.SYSTOLIC_RANGE.first} " +
                    "and ${BloodPressureInputConstraints.SYSTOLIC_RANGE.last}"

            state.diastolic !in BloodPressureInputConstraints.DIASTOLIC_RANGE ->
                "Diastolic must be between ${BloodPressureInputConstraints.DIASTOLIC_RANGE.first} " +
                    "and ${BloodPressureInputConstraints.DIASTOLIC_RANGE.last}"

            state.pulse !in BloodPressureInputConstraints.PULSE_RANGE ->
                "Pulse must be between ${BloodPressureInputConstraints.PULSE_RANGE.first} " +
                    "and ${BloodPressureInputConstraints.PULSE_RANGE.last}"

            else -> null
        }
}
