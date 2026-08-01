package com.tdev.heartrate.shared.domain.usecase

import com.tdev.heartrate.shared.domain.model.HeartRateRecord
import com.tdev.heartrate.shared.domain.repository.HeartRateRepository

class GetHeartRateRecordUseCase(
    private val repository: HeartRateRepository
) {
    suspend operator fun invoke(id: Long): HeartRateRecord? = repository.getRecordById(id)
}
