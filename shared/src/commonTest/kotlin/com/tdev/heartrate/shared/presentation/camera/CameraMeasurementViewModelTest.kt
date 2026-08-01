package com.tdev.heartrate.shared.presentation.camera

import com.tdev.heartrate.shared.domain.sensor.CameraHeartRateSensor
import com.tdev.heartrate.shared.domain.sensor.CameraMeasurementState
import com.tdev.heartrate.shared.domain.sensor.SensorState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.async
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraMeasurementViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        kotlinx.coroutines.Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        kotlinx.coroutines.Dispatchers.resetMain()
    }

    @Test
    fun startMapsSensorEmissionToUiState() = runTest {
        val sensor = FakeCameraHeartRateSensor()
        val viewModel = CameraMeasurementViewModel(sensor)

        viewModel.onIntent(CameraMeasurementIntent.Start)
        advanceUntilIdle()
        sensor.states.emit(CameraMeasurementState(bpm = 72, state = SensorState.MEASURING, progress = 0.4f))
        advanceUntilIdle()

        assertEquals(72, viewModel.uiState.value.measurement.bpm)
        assertEquals(SensorState.MEASURING, viewModel.uiState.value.measurement.state)
        assertEquals(0.4f, viewModel.uiState.value.measurement.progress)
    }

    @Test
    fun completedMeasurementEmitsSideEffectOnceAndStopsSensor() = runTest {
        val sensor = FakeCameraHeartRateSensor()
        val viewModel = CameraMeasurementViewModel(sensor)
        val sideEffect = backgroundScope.async { viewModel.sideEffect.first() }

        viewModel.onIntent(CameraMeasurementIntent.Start)
        advanceUntilIdle()
        sensor.states.emit(CameraMeasurementState(bpm = 76, state = SensorState.COMPLETED, progress = 1f))
        advanceUntilIdle()

        assertEquals(CameraMeasurementSideEffect.Completed(76), sideEffect.await())
        assertEquals(1, sensor.stopCalls)
    }

    @Test
    fun stopIntentStopsSensorAndCancelsMeasurementSession() = runTest {
        val sensor = FakeCameraHeartRateSensor()
        val viewModel = CameraMeasurementViewModel(sensor)

        viewModel.onIntent(CameraMeasurementIntent.Start)
        advanceUntilIdle()
        viewModel.onIntent(CameraMeasurementIntent.Stop)
        advanceUntilIdle()

        assertEquals(1, sensor.stopCalls)
    }
}

private class FakeCameraHeartRateSensor : CameraHeartRateSensor {
    val states = MutableSharedFlow<CameraMeasurementState>(extraBufferCapacity = 8)
    var stopCalls: Int = 0
        private set

    override fun startMeasurement(): Flow<CameraMeasurementState> = states

    override fun stopMeasurement() {
        stopCalls += 1
    }
}
