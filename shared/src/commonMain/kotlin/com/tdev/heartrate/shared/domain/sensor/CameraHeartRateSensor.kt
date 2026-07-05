package com.tdev.heartrate.shared.domain.sensor

import kotlinx.coroutines.flow.Flow

enum class SensorState {
    INITIALIZING,
    NO_FINGER,
    MEASURING,
    COMPLETED,
    FAILED,
    ERROR
}

data class CameraMeasurementState(
    val bpm: Int = 0,
    val state: SensorState = SensorState.INITIALIZING,
    val progress: Float = 0f,
    val errorMessage: String? = null
)

interface CameraHeartRateSensor {
    fun startMeasurement(): Flow<CameraMeasurementState>
    fun stopMeasurement()
}
