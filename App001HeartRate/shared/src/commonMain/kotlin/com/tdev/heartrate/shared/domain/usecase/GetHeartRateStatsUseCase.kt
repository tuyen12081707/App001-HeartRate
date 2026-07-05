package com.tdev.heartrate.shared.domain.usecase

import com.tdev.heartrate.shared.domain.model.HeartRateStats
import com.tdev.heartrate.shared.domain.repository.HeartRateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.math.roundToInt

class GetHeartRateStatsUseCase(
    private val repository: HeartRateRepository
) {
    operator fun invoke(): Flow<HeartRateStats> {
        return repository.getAllRecords().map { records ->
            if (records.isEmpty()) {
                HeartRateStats()
            } else {
                val bpms = records.map { it.bpm }
                HeartRateStats(
                    averageBpm = bpms.average().roundToInt(),
                    maxBpm = bpms.maxOrNull() ?: 0,
                    minBpm = bpms.minOrNull() ?: 0,
                    totalRecords = bpms.size
                )
            }
        }
    }
}
