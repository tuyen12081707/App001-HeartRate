package com.tdev.heartrate.shared.domain.usecase

import com.tdev.heartrate.shared.domain.model.BodyState
import com.tdev.heartrate.shared.domain.model.HeartRateRecord
import com.tdev.heartrate.shared.domain.model.MeasureType
import com.tdev.heartrate.shared.domain.repository.HeartRateRepository


class AddHeartRateRecordUseCase(
    private val repository: HeartRateRepository
) {
    suspend operator fun invoke(
        bpm: Int,
        measureType: MeasureType = MeasureType.MANUAL,
        bodyState: BodyState,
        note: String? = null
    ) {
        val record = HeartRateRecord(
            bpm = bpm,
            timestamp = com.tdev.heartrate.shared.domain.utils.getCurrentTimeMillis(),
            measureType = measureType,
            bodyState = bodyState,
            note = note
        )
        repository.insertRecord(record)
    }
}
