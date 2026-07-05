package com.tdev.heartrate.shared.domain.usecase

import com.tdev.heartrate.shared.domain.repository.HeartRateRepository

class DeleteHeartRateRecordUseCase(
    private val repository: HeartRateRepository
) {
    suspend operator fun invoke(id: Long) {
        repository.deleteRecord(id)
    }
}
