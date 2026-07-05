package com.tdev.heartrate.shared.domain.usecase

import com.tdev.heartrate.shared.domain.model.HeartRateRecord
import com.tdev.heartrate.shared.domain.repository.HeartRateRepository
import kotlinx.coroutines.flow.Flow

class GetHeartRateHistoryUseCase(
    private val repository: HeartRateRepository
) {
    operator fun invoke(): Flow<List<HeartRateRecord>> {
        return repository.getAllRecords()
    }
}
