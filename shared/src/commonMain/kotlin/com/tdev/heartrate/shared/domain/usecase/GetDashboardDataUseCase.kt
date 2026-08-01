package com.tdev.heartrate.shared.domain.usecase

import com.tdev.heartrate.shared.domain.model.DashboardData
import com.tdev.heartrate.shared.domain.model.DashboardPoint
import com.tdev.heartrate.shared.domain.repository.HeartRateRepository
import com.tdev.heartrate.shared.domain.utils.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.math.roundToInt

class GetDashboardDataUseCase(
    private val repository: HeartRateRepository,
    private val clock: Clock
) {
    operator fun invoke(): Flow<DashboardData> = repository.getAllRecords().map { records ->
        val currentDayStart = dayStart(clock.nowMillis())
        val firstDayStart = currentDayStart - (DASHBOARD_DAY_COUNT - 1) * DAY_MILLIS
        val nextDayStart = currentDayStart + DAY_MILLIS
        val recentRecords = records.filter { it.timestamp in firstDayStart until nextDayStart }

        if (recentRecords.isEmpty()) {
            DashboardData(
                latest = null,
                averageBpm = 0,
                minBpm = 0,
                maxBpm = 0,
                totalRecords = 0,
                points = emptyList()
            )
        } else {
            val bpms = recentRecords.map { it.bpm }
            val points = recentRecords
                .groupBy { dayStart(it.timestamp) }
                .entries
                .sortedBy { it.key }
                .map { (dayStartMillis, dayRecords) ->
                    DashboardPoint(
                        dayStartMillis = dayStartMillis,
                        averageBpm = dayRecords.map { it.bpm }.average().roundToInt(),
                        recordCount = dayRecords.size
                    )
                }

            DashboardData(
                latest = recentRecords.maxByOrNull { it.timestamp },
                averageBpm = bpms.average().roundToInt(),
                minBpm = bpms.minOrNull() ?: 0,
                maxBpm = bpms.maxOrNull() ?: 0,
                totalRecords = recentRecords.size,
                points = points
            )
        }
    }

    private fun dayStart(timestamp: Long): Long = (timestamp / DAY_MILLIS) * DAY_MILLIS

    private companion object {
        const val DASHBOARD_DAY_COUNT = 7L
        const val DAY_MILLIS = 86_400_000L
    }
}
