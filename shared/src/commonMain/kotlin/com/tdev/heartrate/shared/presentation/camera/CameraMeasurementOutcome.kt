package com.tdev.heartrate.shared.presentation.camera

import com.tdev.heartrate.shared.domain.sensor.CameraMeasurementState
import com.tdev.heartrate.shared.domain.sensor.SensorState

sealed interface CameraMeasurementOutcome {
    data object None : CameraMeasurementOutcome
    data class Completed(val bpm: Int) : CameraMeasurementOutcome
    data object Failed : CameraMeasurementOutcome
}

fun CameraMeasurementState.toOutcome(): CameraMeasurementOutcome = when {
    state == SensorState.COMPLETED && bpm > 0 -> CameraMeasurementOutcome.Completed(bpm)
    state == SensorState.FAILED || state == SensorState.ERROR -> CameraMeasurementOutcome.Failed
    else -> CameraMeasurementOutcome.None
}
