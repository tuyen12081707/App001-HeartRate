package com.tdev.heartrate.shared.domain.usecase

import com.tdev.heartrate.shared.domain.model.BodyState
import com.tdev.heartrate.shared.domain.model.HeartRateRecord
import com.tdev.heartrate.shared.domain.model.MeasureType
import com.tdev.heartrate.shared.domain.repository.DemoSeedRepository
import com.tdev.heartrate.shared.domain.utils.Clock

class SeedDemoHeartRateUseCase(
    private val demoSeedRepository: DemoSeedRepository,
    private val clock: Clock
) {
    suspend operator fun invoke(): Boolean {
        val now = clock.nowMillis()
        val records = DEMO_BPMS.mapIndexed { index, bpm ->
                HeartRateRecord(
                    bpm = bpm,
                    timestamp = now - (DEMO_BPMS.lastIndex - index) * DAY_MILLIS,
                    measureType = MeasureType.MANUAL,
                    bodyState = BodyState.RESTING
                )
        }
        return demoSeedRepository.seedIfAbsent(
            markerKey = SEED_MARKER_KEY,
            markerValue = SEED_MARKER_VALUE,
            records = records
        )
    }

    private companion object {
        const val SEED_MARKER_KEY = "demo_seed_v1"
        const val SEED_MARKER_VALUE = "true"
        const val DAY_MILLIS = 86_400_000L
        val DEMO_BPMS = listOf(68, 72, 70, 75, 73, 71, 74)
    }
}
