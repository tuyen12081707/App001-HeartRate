package com.tdev.heartrate.shared.domain.usecase

import com.tdev.heartrate.shared.domain.model.BodyState
import com.tdev.heartrate.shared.domain.model.HeartRateRecord
import com.tdev.heartrate.shared.domain.model.MeasureType
import com.tdev.heartrate.shared.domain.repository.HeartRateRepository
import com.tdev.heartrate.shared.domain.utils.Clock

class AddHeartRateRecordUseCase(
    private val repository: HeartRateRepository,
    private val clock: Clock
) {
    suspend operator fun invoke(
        bpm: Int,
        measureType: MeasureType = MeasureType.MANUAL,
        bodyState: BodyState,
        note: String? = null,
        timestamp: Long? = null
    ): Long {
        val record = HeartRateRecord(
            bpm = bpm,
            timestamp = timestamp ?: clock.nowMillis(),
            measureType = measureType,
            bodyState = bodyState,
            note = note
        )
        return repository.insertRecord(record)
    }
}
