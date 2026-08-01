package com.tdev.heartrate.shared.domain.usecase

import com.tdev.heartrate.shared.domain.model.BodyState
import com.tdev.heartrate.shared.domain.model.HeartRateRecord
import com.tdev.heartrate.shared.domain.model.MeasureType
import com.tdev.heartrate.shared.domain.repository.AppMetadataRepository
import com.tdev.heartrate.shared.domain.repository.HeartRateRepository
import com.tdev.heartrate.shared.domain.utils.Clock

class SeedDemoHeartRateUseCase(
    private val heartRateRepository: HeartRateRepository,
    private val metadataRepository: AppMetadataRepository,
    private val clock: Clock
) {
    suspend operator fun invoke(): Boolean {
        if (metadataRepository.get(SEED_MARKER_KEY) != null) return false

        val now = clock.nowMillis()
        DEMO_BPMS.forEachIndexed { index, bpm ->
            heartRateRepository.insertRecord(
                HeartRateRecord(
                    bpm = bpm,
                    timestamp = now - (DEMO_BPMS.lastIndex - index) * DAY_MILLIS,
                    measureType = MeasureType.MANUAL,
                    bodyState = BodyState.RESTING
                )
            )
        }
        metadataRepository.put(SEED_MARKER_KEY, SEED_MARKER_VALUE)
        return true
    }

    private companion object {
        const val SEED_MARKER_KEY = "demo_seed_v1"
        const val SEED_MARKER_VALUE = "true"
        const val DAY_MILLIS = 86_400_000L
        val DEMO_BPMS = listOf(68, 72, 70, 75, 73, 71, 74)
    }
}
