package com.tdev.heartrate.shared.domain.usecase

import com.tdev.heartrate.shared.domain.model.BloodPressureRecord
import com.tdev.heartrate.shared.domain.repository.BloodPressureRepository
import com.tdev.heartrate.shared.domain.utils.getCurrentTimeMillis

class AddBloodPressureRecordUseCase(
    private val repository: BloodPressureRepository
) {
    suspend operator fun invoke(
        systolic: Int,
        diastolic: Int,
        pulse: Int,
        note: String? = null
    ) {
        repository.insertRecord(
            BloodPressureRecord(
                systolic = systolic,
                diastolic = diastolic,
                pulse = pulse,
                timestamp = getCurrentTimeMillis(),
                note = note
            )
        )
    }
}
