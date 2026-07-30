package com.tdev.heartrate.shared.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class BloodPressureRecordTest {

    @Test
    fun classifiesHypotensionWhenEitherReadingIsLow() {
        assertEquals(BloodPressureLevel.HYPOTENSION, record(89, 70).level())
        assertEquals(BloodPressureLevel.HYPOTENSION, record(120, 59).level())
    }

    @Test
    fun classifiesNormalReading() {
        assertEquals(BloodPressureLevel.NORMAL, record(119, 79).level())
    }

    @Test
    fun classifiesHypertensionStagesByHighestReading() {
        assertEquals(BloodPressureLevel.HYPERTENSION_STAGE_1, record(130, 70).level())
        assertEquals(BloodPressureLevel.HYPERTENSION_STAGE_1, record(120, 80).level())
        assertEquals(BloodPressureLevel.HYPERTENSION_STAGE_2, record(140, 80).level())
        assertEquals(BloodPressureLevel.HYPERTENSION_STAGE_2, record(130, 90).level())
    }

    @Test
    fun classifiesHypertensiveCrisisWhenEitherReadingExceedsLimit() {
        assertEquals(BloodPressureLevel.HYPERTENSIVE_CRISIS, record(181, 80).level())
        assertEquals(BloodPressureLevel.HYPERTENSIVE_CRISIS, record(130, 121).level())
    }

    private fun record(systolic: Int, diastolic: Int) =
        BloodPressureRecord(
            systolic = systolic,
            diastolic = diastolic,
            pulse = 70,
            timestamp = 0L
        )
}
