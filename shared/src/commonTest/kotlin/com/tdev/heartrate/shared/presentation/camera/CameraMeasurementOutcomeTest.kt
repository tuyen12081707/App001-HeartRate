package com.tdev.heartrate.shared.presentation.camera

import com.tdev.heartrate.shared.domain.sensor.CameraMeasurementState
import com.tdev.heartrate.shared.domain.sensor.SensorState
import kotlin.test.Test
import kotlin.test.assertEquals

class CameraMeasurementOutcomeTest {
    @Test
    fun completedStateWithBpmProducesCompletedOutcome() {
        assertEquals(
            CameraMeasurementOutcome.Completed(76),
            CameraMeasurementState(bpm = 76, state = SensorState.COMPLETED).toOutcome()
        )
    }

    @Test
    fun errorAndFailedStatesProduceFailedOutcome() {
        assertEquals(
            CameraMeasurementOutcome.Failed,
            CameraMeasurementState(state = SensorState.ERROR).toOutcome()
        )
        assertEquals(
            CameraMeasurementOutcome.Failed,
            CameraMeasurementState(state = SensorState.FAILED).toOutcome()
        )
    }
}
